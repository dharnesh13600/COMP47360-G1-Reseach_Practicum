"""
Performance tests for the ML prediction service.
"""

import pytest
import time
import psutil
import os
import threading
import numpy as np
from fastapi.testclient import TestClient
from unittest.mock import patch
from main import app
from concurrent.futures import ThreadPoolExecutor, as_completed


class TestMLServicePerformance:
    """Test performance characteristics of the ML service."""
    
    @pytest.fixture
    def client(self):
        """Create test client."""
        return TestClient(app)
    
    @pytest.fixture
    def performance_monitor(self):
        """Monitor system performance during tests."""
        class PerformanceMonitor:
            def __init__(self):
                self.process = psutil.Process(os.getpid())
                self.start_time = None
                self.start_memory = None
                self.start_cpu = None
            
            def start(self):
                self.start_time = time.time()
                self.start_memory = self.process.memory_info().rss
                self.start_cpu = self.process.cpu_percent()
                
            def stop(self):
                end_time = time.time()
                end_memory = self.process.memory_info().rss
                end_cpu = self.process.cpu_percent()
                
                return {
                    "duration_ms": (end_time - self.start_time) * 1000,
                    "memory_used_mb": (end_memory - self.start_memory) / (1024 * 1024),
                    "peak_memory_mb": self.process.memory_info().rss / (1024 * 1024),
                    "cpu_usage_percent": end_cpu
                }
        
        return PerformanceMonitor()

    @pytest.mark.performance
    def test_single_prediction_response_time(self, client, performance_monitor):
        """Test response time for single prediction."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            performance_monitor.start()
            response = client.post("/predict_batch", json=request_data)
            metrics = performance_monitor.stop()
            
            assert response.status_code == 200
            assert metrics["duration_ms"] < 1000  # Less than 1 second
            print(f"Single prediction took {metrics['duration_ms']:.2f}ms")

    @pytest.mark.performance
    def test_batch_prediction_scaling(self, client, performance_monitor):
        """Test how performance scales with batch size."""
        batch_sizes = [1, 10, 50, 100, 500, 1000]
        results = []
        
        for batch_size in batch_sizes:
            # Create batch request
            batch_request = []
            for i in range(batch_size):
                batch_request.append({
                    "latitude": 40.7589 + (i * 0.001),
                    "longitude": -73.9851 + (i * 0.001),
                    "hour": (i % 24),
                    "month": 7,
                    "day": 18,
                    "cultural_activity_prefered": f"Activity {i % 10}"
                })
            
            with patch('main.model') as mock_model:
                mock_predictions = np.random.rand(batch_size, 3) * 10
                mock_model.predict.return_value = mock_predictions
                
                performance_monitor.start()
                response = client.post("/predict_batch", json=batch_request)
                metrics = performance_monitor.stop()
                
                assert response.status_code == 200
                
                result = {
                    "batch_size": batch_size,
                    "response_time_ms": metrics["duration_ms"],
                    "memory_used_mb": metrics["memory_used_mb"],
                    "throughput": batch_size / (metrics["duration_ms"] / 1000)  # predictions per second
                }
                results.append(result)
                
                print(f"Batch size {batch_size}: {metrics['duration_ms']:.2f}ms, "
                      f"Throughput: {result['throughput']:.2f} pred/sec")
        
        # Assert performance doesn't degrade dramatically
        small_batch_time = next(r["response_time_ms"] for r in results if r["batch_size"] == 10)
        large_batch_time = next(r["response_time_ms"] for r in results if r["batch_size"] == 1000)
        
        # Large batch should not be more than 50x slower per item
        time_per_item_small = small_batch_time / 10
        time_per_item_large = large_batch_time / 1000
        
        assert time_per_item_large < time_per_item_small * 50

    @pytest.mark.performance
    @pytest.mark.slow
    def test_concurrent_request_performance(self, client):
        """Test performance under concurrent load."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        def make_request():
            with patch('main.model') as mock_model:
                mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
                start_time = time.time()
                response = client.post("/predict_batch", json=request_data)
                end_time = time.time()
                return {
                    "status_code": response.status_code,
                    "response_time": (end_time - start_time) * 1000
                }
        
        # Test with increasing concurrent users
        concurrent_users = [1, 5, 10, 25, 50]
        
        for user_count in concurrent_users:
            start_time = time.time()
            
            with ThreadPoolExecutor(max_workers=user_count) as executor:
                futures = [executor.submit(make_request) for _ in range(user_count)]
                results = [future.result() for future in as_completed(futures)]
            
            total_time = time.time() - start_time
            
            success_count = sum(1 for r in results if r["status_code"] == 200)
            avg_response_time = sum(r["response_time"] for r in results) / len(results)
            throughput = user_count / total_time
            
            print(f"Concurrent users: {user_count}, Success rate: {success_count/user_count*100:.1f}%, "
                  f"Avg response time: {avg_response_time:.2f}ms, Throughput: {throughput:.2f} req/sec")
            
            # Assert acceptable performance
            assert success_count >= user_count * 0.95  # 95% success rate
            assert avg_response_time < 5000  # Less than 5 seconds average

    @pytest.mark.performance
    def test_memory_usage_under_load(self, client, performance_monitor):
        """Test memory usage patterns under various loads."""
        initial_memory = psutil.Process(os.getpid()).memory_info().rss / (1024 * 1024)
        
        # Test different batch sizes
        batch_sizes = [1, 50, 100, 500, 1000]
        memory_usage = []
        
        for batch_size in batch_sizes:
            batch_request = []
            for i in range(batch_size):
                batch_request.append({
                    "latitude": 40.7589 + (i * 0.0001),
                    "longitude": -73.9851 + (i * 0.0001),
                    "hour": i % 24,
                    "month": 7,
                    "day": 18,
                    "cultural_activity_prefered": f"Activity {i % 10}"
                })
            
            with patch('main.model') as mock_model:
                mock_predictions = np.random.rand(batch_size, 3) * 10
                mock_model.predict.return_value = mock_predictions
                
                pre_request_memory = psutil.Process(os.getpid()).memory_info().rss / (1024 * 1024)
                
                response = client.post("/predict_batch", json=batch_request)
                
                post_request_memory = psutil.Process(os.getpid()).memory_info().rss / (1024 * 1024)
                
                memory_increase = post_request_memory - pre_request_memory
                memory_usage.append({
                    "batch_size": batch_size,
                    "memory_increase_mb": memory_increase,
                    "total_memory_mb": post_request_memory
                })
                
                assert response.status_code == 200
        
        # Memory usage should be reasonable
        max_memory_increase = max(m["memory_increase_mb"] for m in memory_usage)
        final_memory = psutil.Process(os.getpid()).memory_info().rss / (1024 * 1024)
        
        print(f"Initial memory: {initial_memory:.1f}MB, Final memory: {final_memory:.1f}MB")
        print(f"Max memory increase: {max_memory_increase:.1f}MB")
        
        # Should not increase memory by more than 500MB during testing
        assert (final_memory - initial_memory) < 500

    @pytest.mark.performance
    def test_model_inference_time(self, client):
        """Test the time spent in model inference specifically."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        inference_times = []
        
        with patch('main.model') as mock_model:
            def timed_predict(*args, **kwargs):
                # Simulate actual model inference time
                time.sleep(0.05)  # 50ms simulation
                return np.array([[25.0, 3.5, 7.8]])
            
            mock_model.predict.side_effect = timed_predict
            
            # Test multiple times to get average
            for _ in range(10):
                start_time = time.time()
                response = client.post("/predict_batch", json=request_data)
                end_time = time.time()
                
                inference_times.append((end_time - start_time) * 1000)
                assert response.status_code == 200
        
        avg_inference_time = sum(inference_times) / len(inference_times)
        min_inference_time = min(inference_times)
        max_inference_time = max(inference_times)
        
        print(f"Model inference - Avg: {avg_inference_time:.2f}ms, "
              f"Min: {min_inference_time:.2f}ms, Max: {max_inference_time:.2f}ms")
        
        # Should be reasonably fast
        assert avg_inference_time < 1000  # Less than 1 second
        assert max_inference_time < 2000  # No request over 2 seconds

    @pytest.mark.performance
    def test_cpu_usage_under_load(self, client):
        """Test CPU usage patterns during intensive processing."""
        import multiprocessing
        
        cpu_count = multiprocessing.cpu_count()
        process = psutil.Process(os.getpid())
        
        # Create large batch request
        large_batch = []
        for i in range(1000):
            large_batch.append({
                "latitude": 40.7589 + (i * 0.0001),
                "longitude": -73.9851 + (i * 0.0001),
                "hour": i % 24,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": f"Activity {i % 10}"
            })
        
        with patch('main.model') as mock_model:
            # Simulate CPU-intensive model prediction
            def cpu_intensive_predict(*args, **kwargs):
                # Simulate some CPU work
                for _ in range(1000):
                    _ = sum(range(100))
                return np.random.rand(1000, 3) * 10
            
            mock_model.predict.side_effect = cpu_intensive_predict
            
            # Monitor CPU before request
            process.cpu_percent()  # Initialize
            time.sleep(0.1)
            baseline_cpu = process.cpu_percent()
            
            start_time = time.time()
            response = client.post("/predict_batch", json=large_batch)
            end_time = time.time()
            
            # Monitor CPU after request
            peak_cpu = process.cpu_percent()
            
            assert response.status_code == 200
            
            print(f"CPU usage - Baseline: {baseline_cpu:.1f}%, Peak: {peak_cpu:.1f}%")
            print(f"Processing time: {(end_time - start_time) * 1000:.2f}ms")
            
            # CPU usage should be reasonable (not consuming all cores)
            max_expected_cpu = min(100, cpu_count * 25)  # Max 25% per core or 100% total
            assert peak_cpu <= max_expected_cpu

    @pytest.mark.performance
    def test_response_time_consistency(self, client):
        """Test consistency of response times."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        response_times = []
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            # Make many requests to test consistency
            for i in range(50):
                start_time = time.time()
                response = client.post("/predict_batch", json=request_data)
                end_time = time.time()
                
                response_time = (end_time - start_time) * 1000
                response_times.append(response_time)
                
                assert response.status_code == 200
        
        avg_time = sum(response_times) / len(response_times)
        std_dev = np.std(response_times)
        min_time = min(response_times)
        max_time = max(response_times)
        
        print(f"Response time stats - Avg: {avg_time:.2f}ms, StdDev: {std_dev:.2f}ms")
        print(f"Min: {min_time:.2f}ms, Max: {max_time:.2f}ms")
        
        # Standard deviation should be reasonable (not too much variance)
        assert std_dev < avg_time * 0.5  # Standard deviation is less than 50% of average
        assert max_time < avg_time * 3    # No outliers more than 3x average

    @pytest.mark.performance
    @pytest.mark.slow
    def test_sustained_load_performance(self, client):
        """Test performance under sustained load over time."""
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        duration_seconds = 30  # 30 second test
        request_interval = 0.1  # 10 requests per second
        
        start_time = time.time()
        response_times = []
        error_count = 0
        request_count = 0
        
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            while time.time() - start_time < duration_seconds:
                request_start = time.time()
                
                try:
                    response = client.post("/predict_batch", json=request_data)
                    request_end = time.time()
                    
                    response_time = (request_end - request_start) * 1000
                    response_times.append(response_time)
                    
                    if response.status_code != 200:
                        error_count += 1
                    
                except Exception:
                    error_count += 1
                
                request_count += 1
                
                # Wait for next request
                time.sleep(max(0, request_interval - (time.time() - request_start)))
        
        total_time = time.time() - start_time
        avg_response_time = sum(response_times) / len(response_times) if response_times else 0
        throughput = request_count / total_time
        error_rate = error_count / request_count if request_count > 0 else 0
        
        print(f"Sustained load test - Duration: {total_time:.1f}s, Requests: {request_count}")
        print(f"Avg response time: {avg_response_time:.2f}ms, Throughput: {throughput:.2f} req/sec")
        print(f"Error rate: {error_rate:.1%}")
        
        # Performance should remain stable
        assert error_rate < 0.05  # Less than 5% error rate
        assert avg_response_time < 1000  # Less than 1 second average
        assert throughput > 5  # At least 5 requests per second

    @pytest.mark.performance
    def test_garbage_collection_impact(self, client):
        """Test the impact of garbage collection on performance."""
        import gc
        
        request_data = [
            {
                "latitude": 40.7589,
                "longitude": -73.9851,
                "hour": 15,
                "month": 7,
                "day": 18,
                "cultural_activity_prefered": "Portrait photography"
            }
        ]
        
        # Measure performance before garbage collection
        with patch('main.model') as mock_model:
            mock_model.predict.return_value = np.array([[25.0, 3.5, 7.8]])
            
            # Warm up
            for _ in range(5):
                client.post("/predict_batch", json=request_data)
            
            # Measure baseline
            baseline_times = []
            for _ in range(10):
                start_time = time.time()
                response = client.post("/predict_batch", json=request_data)
                end_time = time.time()
                baseline_times.append((end_time - start_time) * 1000)
                assert response.status_code == 200
            
            # Force garbage collection
            gc.collect()
            
            # Measure after garbage collection
            post_gc_times = []
            for _ in range(10):
                start_time = time.time()
                response = client.post("/predict_batch", json=request_data)
                end_time = time.time()
                post_gc_times.append((end_time - start_time) * 1000)
                assert response.status_code == 200
        
        baseline_avg = sum(baseline_times) / len(baseline_times)
        post_gc_avg = sum(post_gc_times) / len(post_gc_times)
        
        print(f"Baseline avg: {baseline_avg:.2f}ms, Post-GC avg: {post_gc_avg:.2f}ms")
        
        # Garbage collection shouldn't significantly impact performance
        # Allow to 50%
        assert post_gc_avg < baseline_avg * 1.5

    @pytest.mark.performance
    def test_large_request_payload_limits(self, client):
        """Test behavior with very large request payloads."""
        # Test increasing payload sizes
        payload_sizes = [10, 100, 1000, 5000, 10000]
        
        for size in payload_sizes:
            large_batch = []
            for i in range(size):
                large_batch.append({
                    "latitude": 40.7589 + (i * 0.0001),
                    "longitude": -73.9851 + (i * 0.0001),
                    "hour": i % 24,
                    "month": 7,
                    "day": 18,
                    "cultural_activity_prefered": f"Very long activity name to increase payload size - Activity number {i} with additional descriptive text"
                })
            
            with patch('main.model') as mock_model:
                mock_predictions = np.random.rand(size, 3) * 10
                mock_model.predict.return_value = mock_predictions
                
                start_time = time.time()
                
                try:
                    response = client.post("/predict_batch", json=large_batch)
                    end_time = time.time()
                    
                    response_time = (end_time - start_time) * 1000
                    
                    print(f"Payload size {size}: {response_time:.2f}ms")
                    
                    if size <= 5000:
                        assert response.status_code == 200
                        data = response.json()
                        assert len(data) == size
                    else:  # Very large sizes might fail gracefully
                        assert response.status_code in [200, 413, 422, 500]
                        
                except Exception as e:
                    # Large payloads might cause timeouts or memory errors
                    print(f"Payload size {size} failed: {str(e)}")
                    assert size >= 5000  # Very large sizes should fail