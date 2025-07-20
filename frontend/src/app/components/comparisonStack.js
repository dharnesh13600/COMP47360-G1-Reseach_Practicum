'use client';

import { useState } from 'react';
import '../styles/comparisonStack.css';

export default function ComparisonStack({ stack, clearStack, removeItem }) {
    const [isCollapsed, setIsCollapsed] = useState(false);
    if (!stack.length) return null;

    return (
        <div className={`comparison-panel ${isCollapsed ? 'collapsed' : ''}`}>

            <div className={`comparison-items ${isCollapsed ? 'collapsed' : ''}`}>
                {stack.map(item => (
                    <div key={item.id} className="comparison-item">
                        <button
                            onClick={() => removeItem(item.id)}
                            className="close-item-button"
                        >
                            ✕
                        </button>
                        <p className='loc-name-text'>{item.locName}</p>
                        <p className='muse-score-text'>Muse Score: {item.museScore}/10</p>
                        <p className='est-crowd-text'>Estimate Crowd: {item.estimateCrowd}</p>
                        <p className='crowd-status-text'>Status: {item.crowdStatus}</p>
                    </div>
                ))}
                <button onClick={clearStack} className="clear-button">Clear All</button>
                                                {/* Header with count */}
            <div className="comparison-count">
                {stack.length}/3
            </div>
            </div>
            <div className="panel-header">
                <button
                    onClick={() => setIsCollapsed(prev => !prev)}
                    className="collapse-toggle-button"
                >
                    {isCollapsed ? 'Show Compare' : 'Hide'}
                </button>
            </div>
        </div>
    );
}
