/**
 * ============================================================================
 * CalcPro - Modern Precision Web Calculator
 * Oasis Infobyte Internship - Web Development Level 2, Task 1
 * Author: Mudassir Ahmed
 *
 * Architecture:
 * 1. State Management: Immutable-inspired centralized state machine
 * 2. Tokenizer & Evaluator: Custom Shunting-Yard (RPN) Algorithm (No eval / No new Function)
 * 3. Display Subsystem: Dynamic scaling, dual-line expression history & formatting
 * 4. Input Controllers: Real-time validation, operator chaining & negative numbers
 * 5. Event Listeners: Event delegation & full responsive keyboard accessibility
 * ============================================================================
 */

(function () {
  'use strict';

  /* --------------------------------------------------------------------------
   * 1. State Store
   * -------------------------------------------------------------------------- */
  const state = {
    // Current operand being entered (e.g. "42", "3.14", "-5", "")
    currentInput: '0',

    // History tokens array of infix expression (e.g. ["5", "+", "3", "×"])
    tokens: [],

    // Flag indicating if the most recent action was an evaluation ("=")
    justEvaluated: false,

    // Error status flag (e.g. "Cannot divide by zero" or "Overflow")
    isError: false,

    // Secondary line expression representation
    secondaryText: '',

    // Active operator symbol if an operator was just chosen
    activeOperator: null
  };

  /* --------------------------------------------------------------------------
   * 2. Constants & Operator Maps
   * -------------------------------------------------------------------------- */
  // Display symbols mapping
  const DISPLAY_OPERATORS = {
    '+': '+',
    '-': '−',
    '*': '×',
    '/': '÷'
  };

  // Precedence for Shunting-Yard algorithm (higher = evaluated first)
  const PRECEDENCE = {
    '+': 1,
    '-': 1,
    '*': 2,
    '/': 2
  };

  // Maximum characters allowed in current input to prevent UI overflow
  const MAX_INPUT_LENGTH = 16;

  /* --------------------------------------------------------------------------
   * 3. DOM Elements
   * -------------------------------------------------------------------------- */
  const displayPrimary = document.getElementById('display-primary');
  const displaySecondary = document.getElementById('display-secondary');
  const keypad = document.getElementById('calculator-keypad');

  /* --------------------------------------------------------------------------
   * 4. Custom Evaluator Engine (Shunting-Yard & RPN)
   *    STRICT REQUIREMENT: NO eval() and NO new Function()
   * -------------------------------------------------------------------------- */

  /**
   * Normalize expression tokens to standard arithmetic operators (+, -, *, /)
   * @param {string[]} tokensIn
   * @returns {string[]}
   */
  function normalizeTokens(tokensIn) {
    return tokensIn.map((tok) => {
      if (tok === '×') return '*';
      if (tok === '÷') return '/';
      if (tok === '−') return '-';
      return tok;
    });
  }

  /**
   * Converts Infix token stream into Reverse Polish Notation (Postfix)
   * using Dijkstra's Shunting-Yard Algorithm.
   * Handles operator precedence (× and ÷ before + and −) and left associativity.
   * @param {string[]} infixTokens
   * @returns {(number|string)[]}
   */
  function shuntingYard(infixTokens) {
    const outputQueue = [];
    const operatorStack = [];

    for (const token of infixTokens) {
      const num = Number(token);

      // Check if token is a valid numeric operand
      if (!isNaN(num) && token.trim() !== '') {
        outputQueue.push(num);
      } else if (token in PRECEDENCE) {
        // Operator token: pop operators with greater or equal precedence
        while (
          operatorStack.length > 0 &&
          operatorStack[operatorStack.length - 1] in PRECEDENCE &&
          PRECEDENCE[operatorStack[operatorStack.length - 1]] >= PRECEDENCE[token]
        ) {
          outputQueue.push(operatorStack.pop());
        }
        operatorStack.push(token);
      } else {
        throw new Error(`Invalid token encountered: ${token}`);
      }
    }

    // Drain remaining operators
    while (operatorStack.length > 0) {
      outputQueue.push(operatorStack.pop());
    }

    return outputQueue;
  }

  /**
   * Evaluates an RPN (Reverse Polish Notation) token array.
   * Detects division by zero and numeric bounds.
   * @param {(number|string)[]} rpnTokens
   * @returns {{ value?: number, error?: string }}
   */
  function evaluateRPN(rpnTokens) {
    const stack = [];

    for (const token of rpnTokens) {
      if (typeof token === 'number') {
        stack.push(token);
      } else if (typeof token === 'string' && token in PRECEDENCE) {
        if (stack.length < 2) {
          return { error: 'Invalid expression' };
        }
        const b = stack.pop();
        const a = stack.pop();

        let res = 0;
        switch (token) {
          case '+':
            res = a + b;
            break;
          case '-':
            res = a - b;
            break;
          case '*':
            res = a * b;
            break;
          case '/':
            // Division-by-zero check
            if (b === 0) {
              return { error: 'Cannot divide by zero' };
            }
            res = a / b;
            break;
          default:
            return { error: 'Invalid operator' };
        }
        stack.push(res);
      }
    }

    if (stack.length !== 1) {
      return { error: 'Malformed expression' };
    }

    const finalVal = stack[0];
    if (isNaN(finalVal)) {
      return { error: 'Invalid calculation' };
    }
    if (!isFinite(finalVal)) {
      return { error: 'Overflow' };
    }

    return { value: finalVal };
  }

  /**
   * Evaluates an infix token list with complete operator precedence.
   * @param {string[]} rawTokens
   * @returns {{ value?: number, error?: string }}
   */
  function computeExpression(rawTokens) {
    try {
      const normalized = normalizeTokens(rawTokens);
      const rpn = shuntingYard(normalized);
      return evaluateRPN(rpn);
    } catch (err) {
      return { error: 'Calculation error' };
    }
  }

  /**
   * Formats a raw floating-point number cleanly:
   * - Solves IEEE 754 precision issues (e.g. 0.1 + 0.2 => 0.3)
   * - Rounds to 10-12 significant figures and strips superfluous trailing zeros
   * - Switches to scientific notation for extreme numbers
   * @param {number} num
   * @returns {string}
   */
  function formatNumber(num) {
    if (typeof num !== 'number' || isNaN(num)) {
      return 'Error';
    }
    if (!isFinite(num)) {
      return 'Overflow';
    }

    // Fix floating point representation drifts (e.g. 0.30000000000000004 => 0.3)
    const precision = 12;
    const rounded = Number(Math.round(num + 'e' + precision) + 'e-' + precision);

    // Large or microscopic scientific notation
    if (Math.abs(rounded) >= 1e14 || (Math.abs(rounded) > 0 && Math.abs(rounded) < 1e-6)) {
      return rounded.toExponential(6).replace(/e\+?(-?\d+)/, 'e$1');
    }

    // Normal decimal display - up to 10 decimals, parsed to float to drop trailing zeros
    const fixed = parseFloat(rounded.toFixed(10));
    return fixed.toString();
  }

  /* --------------------------------------------------------------------------
   * 5. Display Formatting & Scaling
   * -------------------------------------------------------------------------- */

  /**
   * Updates the primary and secondary displays based on current state.
   */
  function updateDisplay() {
    // 1. Error state handling
    if (state.isError) {
      displayPrimary.textContent = state.currentInput;
      displayPrimary.className = 'display-primary is-error';
      displaySecondary.textContent = state.secondaryText;
      clearActiveOperatorHighlight();
      displayPrimary.scrollLeft = 0;
      return;
    }

    displayPrimary.classList.remove('is-error');

    // 2. Secondary line text construction
    if (state.justEvaluated) {
      displaySecondary.textContent = state.secondaryText;
    } else if (state.tokens.length > 0) {
      let secondaryContent = state.tokens.join(' ');
      // If user is currently typing a new number, show it or show expression so far
      displaySecondary.textContent = secondaryContent;
    } else {
      displaySecondary.textContent = '';
    }

    // 3. Primary line text construction
    let primaryText = state.currentInput;
    if (primaryText === '') {
      // If user selected an operator and hasn't typed next digit yet,
      // show the previous operand or 0
      if (state.tokens.length > 0) {
        primaryText = state.tokens[state.tokens.length - 2] || state.tokens[0] || '0';
      } else {
        primaryText = '0';
      }
    }

    displayPrimary.textContent = primaryText;
    adjustDisplayFontSize(primaryText);

    // Smooth scroll displays to end so newest input is always visible
    displayPrimary.scrollLeft = displayPrimary.scrollWidth;
    displaySecondary.scrollLeft = displaySecondary.scrollWidth;

    // Highlight active operator if any
    updateActiveOperatorHighlight();
  }

  /**
   * Adjusts the font size class dynamically based on character count.
   * @param {string} text
   */
  function adjustDisplayFontSize(text) {
    const len = text ? text.length : 0;
    displayPrimary.classList.remove('size-medium', 'size-small', 'size-tiny');

    if (len > 15) {
      displayPrimary.classList.add('size-tiny');
    } else if (len > 12) {
      displayPrimary.classList.add('size-small');
    } else if (len > 9) {
      displayPrimary.classList.add('size-medium');
    }
  }

  /**
   * Updates visual active state on operator buttons.
   */
  function updateActiveOperatorHighlight() {
    clearActiveOperatorHighlight();
    if (state.activeOperator && !state.justEvaluated && !state.isError) {
      const activeBtn = document.querySelector(`.btn-operator[data-value="${state.activeOperator}"]`);
      if (activeBtn) {
        activeBtn.classList.add('operator-active');
      }
    }
  }

  function clearActiveOperatorHighlight() {
    const activeBtns = document.querySelectorAll('.btn-operator.operator-active');
    activeBtns.forEach((btn) => btn.classList.remove('operator-active'));
  }

  /* --------------------------------------------------------------------------
   * 6. User Action Handlers
   * -------------------------------------------------------------------------- */

  /**
   * Digit (0-9) input handler
   * Prevents leading zeros ("007" => "7")
   * Recovers from error or starts fresh after evaluation
   * @param {string} digit
   */
  function handleNumber(digit) {
    // If recovering from error, reset first
    if (state.isError) {
      handleClear();
      state.currentInput = digit;
      updateDisplay();
      return;
    }

    // If typing right after '=', start a fresh calculation
    if (state.justEvaluated) {
      state.tokens = [];
      state.secondaryText = '';
      state.currentInput = digit;
      state.justEvaluated = false;
      state.activeOperator = null;
      updateDisplay();
      return;
    }

    // Length boundary protection
    if (state.currentInput.replace('-', '').length >= MAX_INPUT_LENGTH) {
      return;
    }

    // Leading zero replacement
    if (state.currentInput === '0') {
      state.currentInput = digit;
    } else if (state.currentInput === '-0') {
      state.currentInput = '-' + digit;
    } else {
      state.currentInput += digit;
    }

    state.activeOperator = null;
    updateDisplay();
  }

  /**
   * Decimal (.) input handler
   * Prevents multiple decimals ("1.2.3")
   * Converts empty input or leading minus to "0." or "-0."
   */
  function handleDecimal() {
    if (state.isError) {
      handleClear();
      state.currentInput = '0.';
      updateDisplay();
      return;
    }

    if (state.justEvaluated) {
      state.tokens = [];
      state.secondaryText = '';
      state.currentInput = '0.';
      state.justEvaluated = false;
      state.activeOperator = null;
      updateDisplay();
      return;
    }

    // Ignore if current number already has a decimal
    if (state.currentInput.includes('.')) {
      return;
    }

    if (state.currentInput === '' || state.currentInput === '0') {
      state.currentInput = '0.';
    } else if (state.currentInput === '-') {
      state.currentInput = '-0.';
    } else {
      state.currentInput += '.';
    }

    state.activeOperator = null;
    updateDisplay();
  }

  /**
   * Operator (+, -, *, /) input handler
   * Implements operator chaining, operator replacement, and negative number support
   * @param {string} opKey (+, -, *, /)
   */
  function handleOperator(opKey) {
    if (state.isError) {
      // Cannot operate on error message; user should press C or type a digit
      return;
    }

    const displayOp = DISPLAY_OPERATORS[opKey] || opKey;

    // Case 1: Continuing from previous result (e.g. 11 + 4)
    if (state.justEvaluated) {
      state.tokens = [state.currentInput, displayOp];
      state.currentInput = '';
      state.justEvaluated = false;
      state.secondaryText = '';
      state.activeOperator = opKey;
      updateDisplay();
      return;
    }

    // Case 2: Number is currently typed
    if (state.currentInput !== '' && state.currentInput !== '-') {
      state.tokens.push(state.currentInput);
      state.tokens.push(displayOp);
      state.currentInput = '';
      state.activeOperator = opKey;
      updateDisplay();
      return;
    }

    // Case 3: Current input is empty (waiting for number after previous operator)
    if (state.currentInput === '') {
      // 3A: Very beginning of calculation
      if (state.tokens.length === 0) {
        if (opKey === '-') {
          // Leading negative number (e.g. -5 + 3)
          state.currentInput = '-';
          state.activeOperator = null;
        } else {
          // e.g. pressing "+" with nothing typed defaults to "0 +"
          state.tokens.push('0', displayOp);
          state.activeOperator = opKey;
        }
        updateDisplay();
        return;
      }

      // 3B: Expression already ends with an operator
      // Check negative number after multiplication or division (e.g. 5 × -2)
      const lastToken = state.tokens[state.tokens.length - 1];
      if (opKey === '-' && (lastToken === '×' || lastToken === '÷')) {
        state.currentInput = '-';
        state.activeOperator = null;
        updateDisplay();
        return;
      }

      // Operator replacement: replace previous operator instead of stacking
      state.tokens[state.tokens.length - 1] = displayOp;
      state.activeOperator = opKey;
      updateDisplay();
      return;
    }

    // Case 4: Current input is just '-' and user hits another operator
    if (state.currentInput === '-') {
      state.currentInput = '';
      if (state.tokens.length > 0) {
        state.tokens[state.tokens.length - 1] = displayOp;
        state.activeOperator = opKey;
      }
      updateDisplay();
    }
  }

  /**
   * Equals (=) button handler
   * Solves expression with full operator precedence (× and ÷ before + and −)
   */
  function handleEquals() {
    if (state.isError) return;
    if (state.justEvaluated) return;

    // Assemble final token list
    const evalTokens = [...state.tokens];

    if (state.currentInput !== '' && state.currentInput !== '-') {
      evalTokens.push(state.currentInput);
    } else if (evalTokens.length > 0) {
      // If trailing operator with no right operand, remove the dangling operator
      const last = evalTokens[evalTokens.length - 1];
      if (['+', '−', '×', '÷'].includes(last)) {
        evalTokens.pop();
      }
    }

    // Nothing to calculate
    if (evalTokens.length === 0) return;

    // Single number with no operators
    if (evalTokens.length === 1) {
      state.currentInput = evalTokens[0];
      state.secondaryText = `${evalTokens[0]} =`;
      state.tokens = [];
      state.justEvaluated = true;
      state.activeOperator = null;
      updateDisplay();
      return;
    }

    // Construct secondary line text before evaluation
    const expressionString = evalTokens.join(' ') + ' =';

    // Execute computation via custom precedence engine
    const result = computeExpression(evalTokens);

    if (result.error) {
      state.isError = true;
      state.currentInput = result.error;
      state.secondaryText = expressionString;
      state.tokens = [];
      state.justEvaluated = false;
      state.activeOperator = null;
    } else {
      const formatted = formatNumber(result.value);
      state.isError = false;
      state.currentInput = formatted;
      state.secondaryText = expressionString;
      state.tokens = [];
      state.justEvaluated = true;
      state.activeOperator = null;
    }

    updateDisplay();
  }

  /**
   * Clear (C) button handler
   * Resets entire state, display, and expression memory
   */
  function handleClear() {
    state.currentInput = '0';
    state.tokens = [];
    state.justEvaluated = false;
    state.isError = false;
    state.secondaryText = '';
    state.activeOperator = null;
    updateDisplay();
  }

  /**
   * Backspace (⌫) button handler
   * Removes last entered character, recovers cleanly, returns to 0 when empty
   */
  function handleBackspace() {
    // If error, clear error cleanly
    if (state.isError) {
      handleClear();
      return;
    }

    // If just evaluated, clear expression and keep result editable
    if (state.justEvaluated) {
      state.secondaryText = '';
      state.justEvaluated = false;
      updateDisplay();
      return;
    }

    // If currently typing a number
    if (state.currentInput.length > 1) {
      state.currentInput = state.currentInput.slice(0, -1);
      if (state.currentInput === '-') {
        state.currentInput = '0';
      }
    } else if (state.currentInput.length === 1 && state.currentInput !== '0') {
      state.currentInput = '0';
    } else if (state.currentInput === '0' || state.currentInput === '') {
      // If currentInput is empty or 0 and tokens exist, pop operator
      if (state.tokens.length >= 2) {
        state.tokens.pop(); // remove operator
        state.currentInput = state.tokens.pop() || '0'; // bring operand back
        state.activeOperator = null;
      } else if (state.tokens.length === 1) {
        state.currentInput = state.tokens.pop() || '0';
      }
    }

    updateDisplay();
  }

  /**
   * Plus/Minus (±) negation handler
   * Toggles sign of current input
   */
  function handleNegate() {
    if (state.isError) return;

    if (state.justEvaluated) {
      state.tokens = [];
      state.secondaryText = '';
      state.justEvaluated = false;
    }

    if (state.currentInput === '' || state.currentInput === '0') {
      state.currentInput = '-';
    } else if (state.currentInput === '-') {
      state.currentInput = '0';
    } else if (state.currentInput.startsWith('-')) {
      state.currentInput = state.currentInput.slice(1);
    } else {
      state.currentInput = '-' + state.currentInput;
    }

    updateDisplay();
  }

  /* --------------------------------------------------------------------------
   * 7. Event Delegation on Keypad (No inline onclick)
   * -------------------------------------------------------------------------- */
  keypad.addEventListener('click', (event) => {
    const btn = event.target.closest('.calc-btn');
    if (!btn) return;

    const action = btn.dataset.action;
    const value = btn.dataset.value;

    switch (action) {
      case 'number':
        handleNumber(value);
        break;
      case 'decimal':
        handleDecimal();
        break;
      case 'operator':
        handleOperator(value);
        break;
      case 'equals':
        handleEquals();
        break;
      case 'clear':
        handleClear();
        break;
      case 'backspace':
        handleBackspace();
        break;
      case 'negate':
        handleNegate();
        break;
      default:
        break;
    }
  });

  /* --------------------------------------------------------------------------
   * 8. Full Keyboard Navigation & Visual Feedback
   * -------------------------------------------------------------------------- */
  window.addEventListener('keydown', (event) => {
    const key = event.key;
    let handled = false;
    let activeBtnSelector = null;

    // Digits 0 - 9
    if (/^[0-9]$/.test(key)) {
      handleNumber(key);
      activeBtnSelector = `.btn-number[data-value="${key}"]`;
      handled = true;
    } else if (key === '.') {
      handleDecimal();
      activeBtnSelector = '#btn-decimal';
      handled = true;
    } else if (key === '+' || key === '-') {
      handleOperator(key);
      activeBtnSelector = key === '+' ? '#btn-add' : '#btn-subtract';
      handled = true;
    } else if (key === '*' || key === 'x' || key === 'X') {
      handleOperator('*');
      activeBtnSelector = '#btn-multiply';
      handled = true;
    } else if (key === '/') {
      handleOperator('/');
      activeBtnSelector = '#btn-divide';
      handled = true;
    } else if (key === '=' || key === 'Enter') {
      handleEquals();
      activeBtnSelector = '#btn-equals';
      handled = true;
    } else if (key === 'Backspace' || key === 'Delete') {
      handleBackspace();
      activeBtnSelector = '#btn-backspace';
      handled = true;
    } else if (key === 'Escape' || key === 'c' || key === 'C') {
      handleClear();
      activeBtnSelector = '#btn-clear';
      handled = true;
    } else if (key === '_') {
      handleNegate();
      activeBtnSelector = '#btn-negate';
      handled = true;
    }

    if (handled) {
      event.preventDefault();

      // Tactile button animation on keypress
      if (activeBtnSelector) {
        const btn = document.querySelector(activeBtnSelector);
        if (btn) {
          btn.classList.add('keyboard-pressed');
          setTimeout(() => {
            btn.classList.remove('keyboard-pressed');
          }, 120);
        }
      }
    }
  });

  /* --------------------------------------------------------------------------
   * 9. Initial Load Setup
   * -------------------------------------------------------------------------- */
  updateDisplay();
})();
