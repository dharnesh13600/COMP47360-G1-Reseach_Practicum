import { useEffect } from 'react';

/**
 * Detects clicks or taps that occur outside the element referenced by `ref`
 * and then calls `handler`.
 *
 * @param {{ ref: React.RefObject<HTMLElement>, handler: () => void }} params
 */
function useOutsideClick({ ref, handler }) {
  useEffect(() => {
    function handleClickOutside(event) {
      //               ⬑ event is a MouseEvent or TouchEvent
      if (ref.current && !ref.current.contains(event.target)) {
        handler(); // clicked outside → run callback
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('touchstart', handleClickOutside);

    // ✅ cleanup on unmount / dependency change
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('touchstart', handleClickOutside);
    };
  }, [ref, handler]);
}

export default useOutsideClick;
