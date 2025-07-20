'use client';

import { useEffect } from 'react';
import '../styles/comparisonStack.css';

export default function ComparisonStack({ stack, clearStack }) {
  if (!stack.length) return null; // Hide if empty

  return (
    <div className="comparison-panel">
      <div className="panel-header">
        <button onClick={clearStack} className="close-button">Clear</button>
      </div>
      <div className="comparison-items">
        {stack.map(item => (
          <div key={item.id} className="comparison-item">
            <h3 className='loc-name-text'>{item.locName}</h3>
            <p className='muse-score-text'><strong>Muse Score:</strong> {item.museScore}/10</p>
            <p className='est-crowd-text'><strong>Estimate Crowd:</strong> {item.estimateCrowd}</p>
            <p className='crowd-status-text'><strong>Status:</strong> {item.crowdStatus}</p> 
          </div>
        ))}
      </div>
    </div>
  );
}
