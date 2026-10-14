/**
 * ==========================================================================
 * ThermoSwitch - Precision Temperature Converter
 * Oasis Infobyte Web Development Internship (Level 1, Task 3)
 * Author: Mudassir Ahmed
 * ==========================================================================
 */

'use strict';

document.addEventListener('DOMContentLoaded', () => {
  /* ==========================================================================
     1. DOM Element Cache
     ========================================================================== */
  const form = document.getElementById('converter-form');
  const tempInput = document.getElementById('temp-input');
  const inputClearBtn = document.getElementById('input-clear-btn');
  const inputUnitSelect = document.getElementById('input-unit');
  const targetUnitSelect = document.getElementById('target-unit');
  const errorMessageEl = document.getElementById('error-message');
  const convertBtn = document.getElementById('convert-btn');
  const resetBtn = document.getElementById('reset-btn');

  // Results & Display
  const resultsWrapper = document.getElementById('results-wrapper');
  const placeholderState = document.getElementById('placeholder-state');
  const resultsDisplay = document.getElementById('results-display');
  const resultsTimestamp = document.getElementById('results-timestamp');

  // Cards
  const cardCelsius = document.getElementById('card-celsius');
  const cardFahrenheit = document.getElementById('card-fahrenheit');
  const cardKelvin = document.getElementById('card-kelvin');

  const valCelsius = document.getElementById('val-celsius');
  const valFahrenheit = document.getElementById('val-fahrenheit');
  const valKelvin = document.getElementById('val-kelvin');

  const badgeCelsius = document.getElementById('badge-celsius');
  const badgeFahrenheit = document.getElementById('badge-fahrenheit');
  const badgeKelvin = document.getElementById('badge-kelvin');

  // Visualizer & Meta
  const spectrumStatePill = document.getElementById('spectrum-state-pill');
  const spectrumPin = document.getElementById('spectrum-pin');
  const formulaContent = document.getElementById('formula-content');
  const toastEl = document.getElementById('toast-notification');
  const presetChips = document.querySelectorAll('.preset-chip');
  const copyButtons = document.querySelectorAll('.copy-btn');

  /* ==========================================================================
     2. Physical Constants & Limits
     ========================================================================== */
  const ABSOLUTE_ZERO = {
    celsius: -273.15,
    fahrenheit: -459.67,
    kelvin: 0
  };

  const MAX_SAFE_TEMP = 1e12; // 1 trillion degrees safety threshold

  /* ==========================================================================
     3. Pure Mathematical Conversion Functions (Separate Concerns)
     ========================================================================== */
  /**
   * Converts Celsius to Fahrenheit
   * Formula: (C × 9/5) + 32
   * @param {number} c
   * @returns {number}
   */
  function celsiusToFahrenheit(c) {
    return (c * 9 / 5) + 32;
  }

  /**
   * Converts Fahrenheit to Celsius
   * Formula: (F − 32) × 5/9
   * @param {number} f
   * @returns {number}
   */
  function fahrenheitToCelsius(f) {
    return (f - 32) * 5 / 9;
  }

  /**
   * Converts Celsius to Kelvin
   * Formula: C + 273.15
   * @param {number} c
   * @returns {number}
   */
  function celsiusToKelvin(c) {
    return c + 273.15;
  }

  /**
   * Converts Kelvin to Celsius
   * Formula: K − 273.15
   * @param {number} k
   * @returns {number}
   */
  function kelvinToCelsius(k) {
    return k - 273.15;
  }

  /**
   * Converts Fahrenheit to Kelvin
   * Process: convert F to C, then C to K
   * @param {number} f
   * @returns {number}
   */
  function fahrenheitToKelvin(f) {
    const c = fahrenheitToCelsius(f);
    return celsiusToKelvin(c);
  }

  /**
   * Converts Kelvin to Fahrenheit
   * Process: convert K to C, then C to F
   * @param {number} k
   * @returns {number}
   */
  function kelvinToFahrenheit(k) {
    const c = kelvinToCelsius(k);
    return celsiusToFahrenheit(c);
  }

  /**
   * Master converter: converts input value from any unit to all 3 scales.
   * Converts input to Celsius as the central single source of truth.
   * @param {number} value
   * @param {string} fromUnit
   * @returns {{ celsius: number, fahrenheit: number, kelvin: number }}
   */
  function convertAllUnits(value, fromUnit) {
    let c, f, k;

    switch (fromUnit) {
      case 'celsius':
        c = value;
        f = celsiusToFahrenheit(c);
        k = celsiusToKelvin(c);
        break;

      case 'fahrenheit':
        f = value;
        c = fahrenheitToCelsius(f);
        k = fahrenheitToKelvin(f);
        break;

      case 'kelvin':
        k = value;
        c = kelvinToCelsius(k);
        f = kelvinToFahrenheit(k);
        break;

      default:
        throw new Error(`Unsupported unit: ${fromUnit}`);
    }

    return { celsius: c, fahrenheit: f, kelvin: k };
  }

  /* ==========================================================================
     4. Formatting & Utilities
     ========================================================================== */
  /**
   * Rounds a number to at most 2 decimal places with floating-point fix.
   * @param {number} num
   * @returns {number}
   */
  function roundToTwo(num) {
    const rounded = Math.round((num + Number.EPSILON) * 100) / 100;
    return Object.is(rounded, -0) ? 0 : rounded;
  }

  /**
   * Formats a temperature number for display:
   * Rounds to 2 decimal places, formats locale string, avoids trailing ".00"
   * e.g., 32, 273.15, -40, 36.6
   * @param {number} num
   * @returns {string}
   */
  function formatTemperature(num) {
    const rounded = roundToTwo(num);
    return rounded.toLocaleString('en-US', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    });
  }

  /**
   * Computes human thermal sensation and colour pill based on Celsius value
   * @param {number} c
   * @returns {{ text: string, color: string, percentage: number }}
   */
  function getThermalSensation(c) {
    let text = '';
    let color = 'var(--cold-cyan)';

    if (c <= -273.15) {
      text = 'Absolute Zero (0 K)';
      color = '#a855f7';
    } else if (c < -40) {
      text = 'Deep Cryogenic Freeze';
      color = '#38bdf8';
    } else if (c <= 0) {
      text = 'Freezing Point of Water';
      color = '#60a5fa';
    } else if (c < 15) {
      text = 'Cold / Chilly';
      color = '#38bdf8';
    } else if (c <= 25) {
      text = 'Comfortable Room Temp';
      color = '#34d399';
    } else if (c <= 37.5) {
      text = 'Warm / Human Body Temp';
      color = '#f59e0b';
    } else if (c < 100) {
      text = 'Very Hot / Heat Wave';
      color = '#ea580c';
    } else if (c === 100) {
      text = 'Boiling Point of Water';
      color = '#ef4444';
    } else {
      text = 'Extreme Heat / Scorching';
      color = '#ef4444';
    }

    // Map Celsius to pin percentage between -50°C (0%) and 110°C (100%)
    const clampedC = Math.max(-50, Math.min(110, c));
    const percentage = ((clampedC - (-50)) / (110 - (-50))) * 100;

    return { text, color, percentage };
  }

  /* ==========================================================================
     5. Validation Logic & Edge Cases
     ========================================================================== */
  /**
   * Validates temperature string against syntax, non-numeric chars, and physical limits
   * @param {string} rawInput
   * @param {string} unit
   * @returns {{ isValid: boolean, message?: string, value?: number }}
   */
  function validateTemperature(rawInput, unit) {
    const trimmed = rawInput.trim();

    // 1. Reject empty input
    if (trimmed === '') {
      return {
        isValid: false,
        message: 'Please enter a temperature value.'
      };
    }

    // 2. Reject letters, symbols, and malformed decimals (e.g. "12abc", "1.2.3", "--5", "abc")
    // Strictly match optional negative sign followed by digits and optional single decimal point
    const validNumberPattern = /^-?(?:\d+(?:\.\d+)?|\.\d+)$/;
    if (!validNumberPattern.test(trimmed)) {
      return {
        isValid: false,
        message: 'Invalid number format. Only numbers, decimals, and a leading minus sign are allowed (e.g. 25, -40, 98.6).'
      };
    }

    const numericValue = Number(trimmed);

    // 3. Guard against NaN or non-finite values
    if (!Number.isFinite(numericValue)) {
      return {
        isValid: false,
        message: 'Value must be a valid, finite number.'
      };
    }

    // 4. Guard against excessively large numbers
    if (Math.abs(numericValue) > MAX_SAFE_TEMP) {
      return {
        isValid: false,
        message: 'Value exceeds maximum safe calculation range.'
      };
    }

    // 5. Edge case: Physical Absolute Zero check
    const limit = ABSOLUTE_ZERO[unit];
    if (numericValue < limit) {
      let limitLabel = '';
      if (unit === 'celsius') limitLabel = '-273.15°C';
      if (unit === 'fahrenheit') limitLabel = '-459.67°F';
      if (unit === 'kelvin') limitLabel = '0 K';

      return {
        isValid: false,
        message: `Value cannot fall below Absolute Zero (${limitLabel}). Nothing in the universe can be colder.`
      };
    }

    return {
      isValid: true,
      value: numericValue
    };
  }

  /* ==========================================================================
     6. UI Feedback & Rendering
     ========================================================================== */
  /**
   * Displays an accessible error message and highlights the input
   * @param {string} message
   */
  function showError(message) {
    tempInput.classList.add('is-invalid');
    tempInput.setAttribute('aria-invalid', 'true');
    errorMessageEl.innerHTML = `
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      <span>${escapeHtml(message)}</span>
    `;
  }

  /**
   * Clears error states
   */
  function clearError() {
    tempInput.classList.remove('is-invalid');
    tempInput.setAttribute('aria-invalid', 'false');
    errorMessageEl.textContent = '';
  }

  /**
   * Sanitizes text to prevent injection
   * @param {string} str
   * @returns {string}
   */
  function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  /**
   * Updates card badges for Original unit and Target Highlight
   * @param {string} inputUnit
   * @param {string} targetUnit
   */
  function updateCardBadges(inputUnit, targetUnit) {
    const cards = [
      { id: 'celsius', card: cardCelsius, badgeContainer: badgeCelsius },
      { id: 'fahrenheit', card: cardFahrenheit, badgeContainer: badgeFahrenheit },
      { id: 'kelvin', card: cardKelvin, badgeContainer: badgeKelvin }
    ];

    cards.forEach(({ id, card, badgeContainer }) => {
      badgeContainer.innerHTML = '';
      card.classList.remove('is-target-highlight', 'is-original');

      const isOriginal = (id === inputUnit);
      const isTarget = (targetUnit !== 'all' && id === targetUnit);

      if (isOriginal) {
        card.classList.add('is-original');
        const origPill = document.createElement('span');
        origPill.className = 'pill-badge pill-original';
        origPill.textContent = 'Original';
        origPill.title = 'Selected input unit';
        badgeContainer.appendChild(origPill);
      }

      if (isTarget) {
        card.classList.add('is-target-highlight');
        const targetPill = document.createElement('span');
        targetPill.className = 'pill-badge pill-target';
        targetPill.textContent = 'Highlighted';
        targetPill.title = 'Target focus unit';
        badgeContainer.appendChild(targetPill);
      }
    });
  }

  /**
   * Generates step-by-step formula breakdown text
   * @param {number} inputVal
   * @param {string} inputUnit
   * @param {{ celsius: number, fahrenheit: number, kelvin: number }} results
   */
  function renderFormulaBreakdown(inputVal, inputUnit, results) {
    const formattedIn = formatTemperature(inputVal);
    let html = '';

    if (inputUnit === 'celsius') {
      html = `
        <div>&bull; <strong>To Fahrenheit:</strong> (${formattedIn}°C &times; 9/5) + 32 = <strong>${formatTemperature(results.fahrenheit)}°F</strong></div>
        <div>&bull; <strong>To Kelvin:</strong> ${formattedIn}°C + 273.15 = <strong>${formatTemperature(results.kelvin)} K</strong></div>
      `;
    } else if (inputUnit === 'fahrenheit') {
      html = `
        <div>&bull; <strong>To Celsius:</strong> (${formattedIn}°F &minus; 32) &times; 5/9 = <strong>${formatTemperature(results.celsius)}°C</strong></div>
        <div>&bull; <strong>To Kelvin:</strong> (${formattedIn}°F &minus; 32) &times; 5/9 + 273.15 = <strong>${formatTemperature(results.kelvin)} K</strong></div>
      `;
    } else if (inputUnit === 'kelvin') {
      html = `
        <div>&bull; <strong>To Celsius:</strong> ${formattedIn} K &minus; 273.15 = <strong>${formatTemperature(results.celsius)}°C</strong></div>
        <div>&bull; <strong>To Fahrenheit:</strong> (${formattedIn} K &minus; 273.15) &times; 9/5 + 32 = <strong>${formatTemperature(results.fahrenheit)}°F</strong></div>
      `;
    }

    formulaContent.innerHTML = html;
  }

  /**
   * Performs the conversion and updates the full UI
   */
  function handleConvert() {
    clearError();

    const rawValue = tempInput.value;
    const currentInputUnit = inputUnitSelect.value;
    const currentTargetUnit = targetUnitSelect.value;

    // Validate
    const validation = validateTemperature(rawValue, currentInputUnit);
    if (!validation.isValid) {
      showError(validation.message);
      return;
    }

    const numericValue = validation.value;

    // Calculate conversions
    const results = convertAllUnits(numericValue, currentInputUnit);

    // Update display values
    valCelsius.textContent = formatTemperature(results.celsius);
    valFahrenheit.textContent = formatTemperature(results.fahrenheit);
    valKelvin.textContent = formatTemperature(results.kelvin);

    // Update Badges & Target Highlights
    updateCardBadges(currentInputUnit, currentTargetUnit);

    // Update Thermal Sensation Spectrum
    const sensation = getThermalSensation(results.celsius);
    spectrumStatePill.textContent = sensation.text;
    spectrumStatePill.style.color = sensation.color;
    spectrumPin.style.left = `${sensation.percentage}%`;

    // Render Educational Formula Explanation
    renderFormulaBreakdown(numericValue, currentInputUnit, results);

    // Update Timestamp
    const now = new Date();
    resultsTimestamp.textContent = `Calculated at ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}`;

    // Reveal results and hide placeholder
    placeholderState.classList.add('hidden');
    resultsDisplay.classList.remove('hidden');

    // Trigger re-animation smoothly
    resultsDisplay.style.animation = 'none';
    // Force reflow
    void resultsDisplay.offsetWidth;
    resultsDisplay.style.animation = '';
  }

  /**
   * Resets the entire form and returns to initial state
   */
  function handleReset() {
    clearError();
    tempInput.value = '';
    inputClearBtn.classList.remove('visible');
    inputUnitSelect.value = 'celsius';
    targetUnitSelect.value = 'all';

    // Reset presets
    presetChips.forEach(chip => chip.classList.remove('active'));

    // Reset values
    valCelsius.textContent = '--';
    valFahrenheit.textContent = '--';
    valKelvin.textContent = '--';

    // Reset cards styling
    cardCelsius.classList.remove('is-target-highlight', 'is-original');
    cardFahrenheit.classList.remove('is-target-highlight', 'is-original');
    cardKelvin.classList.remove('is-target-highlight', 'is-original');
    badgeCelsius.innerHTML = '';
    badgeFahrenheit.innerHTML = '';
    badgeKelvin.innerHTML = '';

    // Show placeholder, hide results
    resultsDisplay.classList.add('hidden');
    placeholderState.classList.remove('hidden');

    tempInput.focus();
    showToast('Reset completed.');
  }

  /**
   * Displays temporary toast notification
   * @param {string} msg
   */
  let toastTimer = null;
  function showToast(msg) {
    if (toastTimer) clearTimeout(toastTimer);
    toastEl.textContent = msg;
    toastEl.classList.remove('hidden');

    toastTimer = setTimeout(() => {
      toastEl.classList.add('hidden');
    }, 2400);
  }

  /**
   * Copies formatted temperature to clipboard
   * @param {string} targetId
   * @param {string} symbol
   */
  async function copyToClipboard(targetId, symbol) {
    const valEl = document.getElementById(targetId);
    if (!valEl || valEl.textContent === '--') return;

    const copyText = `${valEl.textContent} ${symbol}`.trim();

    try {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        await navigator.clipboard.writeText(copyText);
      } else {
        // Fallback for environments where Clipboard API is restricted
        const textarea = document.createElement('textarea');
        textarea.value = copyText;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
      }
      showToast(`Copied ${copyText} to clipboard!`);
    } catch (err) {
      showToast(`Copied ${copyText}`);
    }
  }

  /* ==========================================================================
     7. Event Listeners (Strictly via addEventListener)
     ========================================================================== */
  // Form submission (Convert button or Enter key inside form)
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    handleConvert();
  });

  // Clear button next to reset
  resetBtn.addEventListener('click', handleReset);

  // Input clear icon
  inputClearBtn.addEventListener('click', () => {
    tempInput.value = '';
    inputClearBtn.classList.remove('visible');
    clearError();
    tempInput.focus();
  });

  // Input typing interactions: toggle clear icon & auto-clear error if user edits
  tempInput.addEventListener('input', () => {
    if (tempInput.value.trim().length > 0) {
      inputClearBtn.classList.add('visible');
    } else {
      inputClearBtn.classList.remove('visible');
    }
    if (tempInput.classList.contains('is-invalid')) {
      clearError();
    }
  });

  // Target unit selector change: dynamically update highlighting if results are visible
  targetUnitSelect.addEventListener('change', () => {
    if (!resultsDisplay.classList.contains('hidden')) {
      updateCardBadges(inputUnitSelect.value, targetUnitSelect.value);
    }
  });

  // Input unit selector change: if already converted, re-run conversion with new unit
  inputUnitSelect.addEventListener('change', () => {
    if (!resultsDisplay.classList.contains('hidden') && tempInput.value.trim() !== '') {
      handleConvert();
    }
  });

  // Quick Benchmark Presets
  presetChips.forEach(chip => {
    chip.addEventListener('click', () => {
      presetChips.forEach(c => c.classList.remove('active'));
      chip.classList.add('active');

      const val = chip.getAttribute('data-val');
      const unit = chip.getAttribute('data-unit');

      tempInput.value = val;
      inputUnitSelect.value = unit;
      inputClearBtn.classList.add('visible');
      clearError();

      handleConvert();
    });
  });

  // Copy buttons
  copyButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-copy-target');
      const symbol = btn.getAttribute('data-symbol');
      copyToClipboard(targetId, symbol);
    });
  });

  // Keyboard accessibility: handle Escape key to clear field
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && document.activeElement === tempInput) {
      tempInput.value = '';
      inputClearBtn.classList.remove('visible');
      clearError();
    }
  });

  // URL Query Parameters support (deep linking for specific conversions or demonstrations)
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has('temp')) {
    tempInput.value = urlParams.get('temp');
    inputClearBtn.classList.add('visible');
    if (urlParams.has('unit') && ['celsius', 'fahrenheit', 'kelvin'].includes(urlParams.get('unit'))) {
      inputUnitSelect.value = urlParams.get('unit');
    }
    if (urlParams.has('target') && ['all', 'celsius', 'fahrenheit', 'kelvin'].includes(urlParams.get('target'))) {
      targetUnitSelect.value = urlParams.get('target');
    }
    handleConvert();
  }
});
