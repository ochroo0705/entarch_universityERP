import { useEffect, useMemo, useRef, useState } from 'react';
import DropdownCaret from './DropdownCaret';

export default function SelectMenu({
  options = [],
  value,
  onChange,
  placeholder = 'Select an option',
  emptyLabel = 'No options found',
  disabled = false,
}) {
  const wrapperRef = useRef(null);
  const buttonRef = useRef(null);
  const [isOpen, setIsOpen] = useState(false);

  const selectedOption = useMemo(
    () => options.find((option) => String(option.value) === String(value)) || null,
    [options, value]
  );

  useEffect(() => {
    const handlePointerDown = (event) => {
      if (!wrapperRef.current?.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handlePointerDown);
    return () => document.removeEventListener('mousedown', handlePointerDown);
  }, []);

  const handleSelect = (option) => {
    onChange?.(option.value, option);
    setIsOpen(false);
    window.requestAnimationFrame(() => {
      buttonRef.current?.blur();
    });
  };

  return (
    <div ref={wrapperRef} className={`searchable-select${disabled ? ' is-disabled' : ''}${isOpen ? ' is-open' : ''}`}>
      <div className="searchable-select-control">
        <button
          ref={buttonRef}
          type="button"
          className="form-control select-menu-trigger"
          onClick={() => !disabled && setIsOpen((current) => !current)}
          disabled={disabled}
          aria-expanded={isOpen}
        >
          <span className={`select-menu-value${selectedOption ? '' : ' is-placeholder'}`}>
            {selectedOption?.label || placeholder}
          </span>
          <span className="searchable-select-toggle is-passive" aria-hidden="true">
            <DropdownCaret isOpen={isOpen} />
          </span>
        </button>
      </div>

      {isOpen ? (
        <div
          className="searchable-select-menu"
          role="listbox"
          aria-label={placeholder}
          onMouseDown={(event) => event.preventDefault()}
          onClick={(event) => event.stopPropagation()}
        >
          {options.length ? (
            options.map((option) => (
              <button
                key={option.value}
                type="button"
                className={`searchable-select-option${String(option.value) === String(value) ? ' is-selected' : ''}`}
                onMouseDown={(event) => event.preventDefault()}
                onClick={(event) => {
                  event.preventDefault();
                  event.stopPropagation();
                  handleSelect(option);
                }}
                role="option"
                aria-selected={String(option.value) === String(value)}
              >
                <span>{option.label}</span>
                {option.meta ? <small>{option.meta}</small> : null}
              </button>
            ))
          ) : (
            <div className="searchable-select-empty">{emptyLabel}</div>
          )}
        </div>
      ) : null}
    </div>
  );
}
