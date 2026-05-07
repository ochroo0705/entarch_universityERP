import { useEffect, useId, useMemo, useRef, useState } from 'react';
import DropdownCaret from './DropdownCaret';

export default function SearchableSelect({
  options = [],
  value,
  onChange,
  searchValue,
  onSearchChange,
  placeholder = 'Select an option',
  searchPlaceholder = 'Search...',
  emptyLabel = 'No options found',
  loadingLabel = 'Loading...',
  isLoading = false,
  disabled = false,
}) {
  const wrapperRef = useRef(null);
  const inputRef = useRef(null);
  const inputId = useId();
  const [isOpen, setIsOpen] = useState(false);
  const [internalSearch, setInternalSearch] = useState('');

  const isSearchControlled = typeof searchValue === 'string';
  const search = isSearchControlled ? searchValue : internalSearch;
  const setSearch = (nextValue) => {
    if (!isSearchControlled) {
      setInternalSearch(nextValue);
    }
    onSearchChange?.(nextValue);
  };

  const selectedOption = useMemo(
    () => options.find((option) => String(option.value) === String(value)) || null,
    [options, value]
  );
  const displayLabel = search || selectedOption?.label || '';

  const filteredOptions = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return options;
    return options.filter((option) => option.label.toLowerCase().includes(term));
  }, [options, search]);

  useEffect(() => {
    if (!isSearchControlled) {
      setInternalSearch('');
    }
  }, [isSearchControlled, selectedOption]);

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
    setSearch('');
    setIsOpen(false);
    window.requestAnimationFrame(() => {
      inputRef.current?.blur();
    });
  };

  return (
    <div ref={wrapperRef} className={`searchable-select${disabled ? ' is-disabled' : ''}${isOpen ? ' is-open' : ''}`}>
      <div className="searchable-select-control">
        <input
          ref={inputRef}
          id={inputId}
          type="text"
          className="form-control searchable-select-input"
          value={displayLabel}
          placeholder={placeholder}
          onFocus={() => {
            if (disabled) return;
            setSearch('');
            setIsOpen(true);
          }}
          onChange={(event) => {
            setSearch(event.target.value);
            if (!disabled) setIsOpen(true);
          }}
          disabled={disabled}
          autoComplete="off"
          aria-expanded={isOpen}
          aria-autocomplete="list"
        />
        <button
          type="button"
          className="searchable-select-toggle"
          onClick={() => !disabled && setIsOpen((current) => !current)}
          disabled={disabled}
          aria-label="Toggle options"
        >
          <DropdownCaret isOpen={isOpen} />
        </button>
      </div>

      {isOpen ? (
        <div
          className="searchable-select-menu"
          role="listbox"
          aria-label={searchPlaceholder}
          onMouseDown={(event) => event.preventDefault()}
          onClick={(event) => event.stopPropagation()}
        >
          {isLoading ? (
            <div className="searchable-select-empty">{loadingLabel}</div>
          ) : filteredOptions.length ? (
            filteredOptions.map((option) => (
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
