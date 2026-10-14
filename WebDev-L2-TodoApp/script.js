/**
 * TaskForge — Modern & Interactive To-Do Web App
 * Oasis Infobyte Internship | Web Development Level 2 Task 3
 * Author: Mudassir Ahmed
 *
 * STRICT TECH CONSTRAINTS:
 * - HTML5, CSS3, and Vanilla JavaScript only.
 * - No frameworks, libraries, jQuery, or Bootstrap.
 * - Event delegation on task lists (data-id & data-action attributes).
 * - Safe DOM manipulation (textContent / createElement, zero innerHTML injection for user content).
 * - State as single source of truth + localStorage persistence with corruption resilience.
 */

'use strict';

// ============================================================================
// 1. Configuration & Constants
// ============================================================================
const STORAGE_KEY = 'taskforge_tasks_v1';
const MAX_TASK_LENGTH = 100;

// ============================================================================
// 2. Application State (Single Source of Truth)
// ============================================================================
let tasks = [];
let editingTaskId = null;
let deletingTaskId = null;

// ============================================================================
// 3. DOM Element References
// ============================================================================
const elements = {
  taskForm: document.getElementById('task-form'),
  taskInput: document.getElementById('task-input'),
  charCounter: document.getElementById('char-counter'),
  inputError: document.getElementById('input-error'),
  pendingList: document.getElementById('pending-list'),
  completedList: document.getElementById('completed-list'),
  pendingCount: document.getElementById('pending-count'),
  completedCount: document.getElementById('completed-count'),
  clearCompletedBtn: document.getElementById('clear-completed-btn'),
  progressBar: document.getElementById('progress-bar'),
  progressFill: document.getElementById('progress-fill'),
  progressPercent: document.getElementById('progress-percent'),
};

// ============================================================================
// 4. Storage & Persistence
// ============================================================================

/**
 * Loads tasks safely from localStorage.
 * Handles missing data or corrupted JSON gracefully with try/catch.
 * @returns {Array} List of valid task objects
 */
function loadTasks() {
  try {
    const rawData = localStorage.getItem(STORAGE_KEY);
    if (!rawData) return [];

    const parsed = JSON.parse(rawData);
    if (Array.isArray(parsed)) {
      // Validate schema for each item
      return parsed.filter(item => {
        return (
          item &&
          typeof item === 'object' &&
          typeof item.id === 'string' &&
          typeof item.text === 'string' &&
          typeof item.completed === 'boolean'
        );
      });
    }
    return [];
  } catch (error) {
    console.warn('TaskForge: Failed to parse localStorage data. Reverting to empty list.', error);
    return [];
  }
}

/**
 * Persists tasks array to localStorage.
 */
function saveTasks() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
  } catch (error) {
    console.error('TaskForge: Unable to save tasks to localStorage.', error);
  }
}

// ============================================================================
// 5. Utility Functions
// ============================================================================

/**
 * Generates a unique identifier (RFC4122 v4 UUID or robust fallback).
 * @returns {string} Unique ID
 */
function generateId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return 'tf-' + Date.now().toString(36) + '-' + Math.random().toString(36).substring(2, 9);
}

/**
 * Formats an ISO date string into a clean, readable timestamp ("10 Oct, 3:45 PM").
 * @param {string} isoString - ISO date string
 * @returns {string} Formatted timestamp
 */
function formatTimestamp(isoString) {
  if (!isoString) return '';
  const date = new Date(isoString);
  if (isNaN(date.getTime())) return '';

  const day = date.getDate();
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  const month = months[date.getMonth()];

  let hours = date.getHours();
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12;
  hours = hours ? hours : 12; // 0 should be 12

  return `${day} ${month}, ${hours}:${minutes} ${ampm}`;
}

/**
 * Creates an inline SVG DOM element safely without innerHTML.
 * @param {string} type - Icon identifier
 * @returns {SVGElement} SVG DOM node
 */
function createSvgElement(type) {
  const svgNS = 'http://www.w3.org/2000/svg';
  const svg = document.createElementNS(svgNS, 'svg');
  svg.setAttribute('aria-hidden', 'true');
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('fill', 'none');
  svg.setAttribute('stroke', 'currentColor');
  svg.setAttribute('stroke-linecap', 'round');
  svg.setAttribute('stroke-linejoin', 'round');

  if (type === 'check') {
    svg.setAttribute('stroke-width', '3');
    const polyline = document.createElementNS(svgNS, 'polyline');
    polyline.setAttribute('points', '20 6 9 17 4 12');
    svg.appendChild(polyline);
  } else if (type === 'edit') {
    svg.setAttribute('stroke-width', '2');
    const path1 = document.createElementNS(svgNS, 'path');
    path1.setAttribute('d', 'M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7');
    const path2 = document.createElementNS(svgNS, 'path');
    path2.setAttribute('d', 'M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z');
    svg.appendChild(path1);
    svg.appendChild(path2);
  } else if (type === 'trash') {
    svg.setAttribute('stroke-width', '2');
    const polyline = document.createElementNS(svgNS, 'polyline');
    polyline.setAttribute('points', '3 6 5 6 21 6');
    const path1 = document.createElementNS(svgNS, 'path');
    path1.setAttribute('d', 'M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2');
    svg.appendChild(polyline);
    svg.appendChild(path1);
  } else if (type === 'save') {
    svg.setAttribute('stroke-width', '2.5');
    const polyline = document.createElementNS(svgNS, 'polyline');
    polyline.setAttribute('points', '20 6 9 17 4 12');
    svg.appendChild(polyline);
  } else if (type === 'cancel') {
    svg.setAttribute('stroke-width', '2.5');
    const line1 = document.createElementNS(svgNS, 'line');
    line1.setAttribute('x1', '18');
    line1.setAttribute('y1', '6');
    line1.setAttribute('x2', '6');
    line1.setAttribute('y2', '18');
    const line2 = document.createElementNS(svgNS, 'line');
    line2.setAttribute('x1', '6');
    line2.setAttribute('y1', '6');
    line2.setAttribute('x2', '18');
    line2.setAttribute('y2', '18');
    svg.appendChild(line1);
    svg.appendChild(line2);
  } else if (type === 'clock') {
    svg.setAttribute('stroke-width', '2');
    const circle = document.createElementNS(svgNS, 'circle');
    circle.setAttribute('cx', '12');
    circle.setAttribute('cy', '12');
    circle.setAttribute('r', '10');
    const polyline = document.createElementNS(svgNS, 'polyline');
    polyline.setAttribute('points', '12 6 12 12 16 14');
    svg.appendChild(circle);
    svg.appendChild(polyline);
  } else if (type === 'check-circle') {
    svg.setAttribute('stroke-width', '2');
    const path = document.createElementNS(svgNS, 'path');
    path.setAttribute('d', 'M22 11.08V12a10 10 0 1 1-5.93-9.14');
    const polyline = document.createElementNS(svgNS, 'polyline');
    polyline.setAttribute('points', '22 4 12 14.01 9 11.01');
    svg.appendChild(path);
    svg.appendChild(polyline);
  }

  return svg;
}

// ============================================================================
// 6. UI Helpers & Validation
// ============================================================================

/**
 * Displays an accessible, friendly inline error message without alert().
 * @param {string} message - Error text
 */
function showInputError(message) {
  elements.inputError.textContent = message;
  elements.taskInput.setAttribute('aria-invalid', 'true');
}

/**
 * Clears the inline error message.
 */
function clearInputError() {
  elements.inputError.textContent = '';
  elements.taskInput.removeAttribute('aria-invalid');
}

/**
 * Updates the live character counter.
 */
function updateCharCounter() {
  const currentLength = elements.taskInput.value.length;
  elements.charCounter.textContent = `${currentLength}/${MAX_TASK_LENGTH}`;

  elements.charCounter.classList.remove('limit-near', 'limit-full');
  if (currentLength >= MAX_TASK_LENGTH) {
    elements.charCounter.classList.add('limit-full');
  } else if (currentLength >= MAX_TASK_LENGTH - 15) {
    elements.charCounter.classList.add('limit-near');
  }
}

// ============================================================================
// 7. Task Actions (Add, Toggle, Edit, Delete, Clear)
// ============================================================================

/**
 * Adds a new task to the top of the pending list.
 * @param {string} rawText - Unsanitized user input
 */
function addTask(rawText) {
  const text = rawText.trim();

  if (!text) {
    showInputError('Please enter a task description before adding.');
    elements.taskInput.focus();
    return;
  }

  if (text.length > MAX_TASK_LENGTH) {
    showInputError(`Task cannot exceed ${MAX_TASK_LENGTH} characters.`);
    elements.taskInput.focus();
    return;
  }

  const newTask = {
    id: generateId(),
    text: text,
    completed: false,
    createdAt: new Date().toISOString(),
    completedAt: null,
  };

  // Add to the top of pending tasks
  tasks.unshift(newTask);

  saveTasks();
  clearInputError();
  elements.taskInput.value = '';
  updateCharCounter();
  render();

  // Refocus input field after adding
  elements.taskInput.focus();
}

/**
 * Toggles a task between pending and completed.
 * @param {string} id - Task ID
 */
function toggleTask(id) {
  const task = tasks.find(t => t.id === id);
  if (!task) return;

  task.completed = !task.completed;
  task.completedAt = task.completed ? new Date().toISOString() : null;

  // If completing or un-completing, close any active edit or delete prompt on this task
  if (editingTaskId === id) editingTaskId = null;
  if (deletingTaskId === id) deletingTaskId = null;

  saveTasks();
  render();
}

/**
 * Enters inline edit mode for a specific task.
 * Closes any previous edit mode automatically (only 1 open at a time).
 * @param {string} id - Task ID
 */
function enterEditMode(id) {
  editingTaskId = id;
  deletingTaskId = null; // Close any delete confirmation
  render();

  // Focus and select the inline edit input
  const editInput = document.querySelector(`.task-edit-input[data-id="${id}"]`);
  if (editInput) {
    editInput.focus();
    editInput.select();
  }
}

/**
 * Cancels inline editing mode without saving changes.
 */
function cancelEditMode() {
  editingTaskId = null;
  render();
}

/**
 * Saves modified task text from inline edit mode.
 * Rejects empty or whitespace-only edits.
 * @param {string} id - Task ID
 * @param {string} newRawText - Updated text
 */
function saveEditTask(id, newRawText) {
  const trimmed = newRawText.trim();
  const task = tasks.find(t => t.id === id);

  if (!task) {
    editingTaskId = null;
    render();
    return;
  }

  if (!trimmed) {
    // Show visual highlight or focus without saving empty
    const editInput = document.querySelector(`.task-edit-input[data-id="${id}"]`);
    if (editInput) {
      editInput.style.borderColor = 'var(--accent-danger)';
      editInput.setAttribute('placeholder', 'Task text cannot be empty!');
      editInput.focus();
    }
    return;
  }

  task.text = trimmed.slice(0, MAX_TASK_LENGTH);
  editingTaskId = null;

  saveTasks();
  render();
}

/**
 * Prompts inline confirmation for deleting a task.
 * @param {string} id - Task ID
 */
function promptDeleteTask(id) {
  deletingTaskId = id;
  editingTaskId = null; // Close edit mode if open
  render();
}

/**
 * Cancels inline deletion prompt.
 */
function cancelDeleteTask() {
  deletingTaskId = null;
  render();
}

/**
 * Permanently removes a task after user confirmation.
 * @param {string} id - Task ID
 */
function confirmDeleteTask(id) {
  tasks = tasks.filter(t => t.id !== id);
  if (deletingTaskId === id) deletingTaskId = null;
  if (editingTaskId === id) editingTaskId = null;

  saveTasks();
  render();
}

/**
 * Removes all completed tasks from the list.
 */
function clearCompletedTasks() {
  tasks = tasks.filter(t => !t.completed);
  editingTaskId = null;
  deletingTaskId = null;

  saveTasks();
  render();
  elements.taskInput.focus();
}

// ============================================================================
// 8. Safe DOM Rendering Engine
// ============================================================================

/**
 * Creates an empty state element for a task list.
 * @param {string} message - Friendly message
 * @param {string} icon - Emoji/icon
 * @returns {HTMLLIElement} Empty state list item
 */
function createEmptyStateItem(message, icon) {
  const li = document.createElement('li');
  li.className = 'empty-state';

  const iconSpan = document.createElement('span');
  iconSpan.className = 'empty-state-icon';
  iconSpan.setAttribute('aria-hidden', 'true');
  iconSpan.textContent = icon;

  const textSpan = document.createElement('span');
  textSpan.className = 'empty-state-text';
  textSpan.textContent = message;

  li.appendChild(iconSpan);
  li.appendChild(textSpan);
  return li;
}

/**
 * Creates an inline edit row for a task.
 * @param {Object} task - Task object
 * @returns {HTMLLIElement} List item with edit inputs
 */
function createEditItemElement(task) {
  const li = document.createElement('li');
  li.className = 'task-item';
  li.dataset.id = task.id;

  const editRow = document.createElement('div');
  editRow.className = 'task-edit-row';

  const input = document.createElement('input');
  input.type = 'text';
  input.className = 'task-edit-input';
  input.value = task.text;
  input.maxLength = MAX_TASK_LENGTH;
  input.dataset.id = task.id;
  input.setAttribute('aria-label', `Edit task: ${task.text}`);

  const saveBtn = document.createElement('button');
  saveBtn.type = 'button';
  saveBtn.className = 'btn btn-save-edit btn-sm';
  saveBtn.dataset.action = 'save';
  saveBtn.dataset.id = task.id;
  saveBtn.setAttribute('aria-label', 'Save changes');

  const saveSvg = createSvgElement('save');
  saveSvg.classList.add('btn-icon-svg');
  const saveText = document.createElement('span');
  saveText.textContent = 'Save';
  saveBtn.appendChild(saveSvg);
  saveBtn.appendChild(saveText);

  const cancelBtn = document.createElement('button');
  cancelBtn.type = 'button';
  cancelBtn.className = 'btn btn-cancel-edit btn-sm';
  cancelBtn.dataset.action = 'cancel';
  cancelBtn.dataset.id = task.id;
  cancelBtn.setAttribute('aria-label', 'Cancel editing');

  const cancelSvg = createSvgElement('cancel');
  cancelSvg.classList.add('btn-icon-svg');
  const cancelText = document.createElement('span');
  cancelText.textContent = 'Cancel';
  cancelBtn.appendChild(cancelSvg);
  cancelBtn.appendChild(cancelText);

  editRow.appendChild(input);
  editRow.appendChild(saveBtn);
  editRow.appendChild(cancelBtn);

  li.appendChild(editRow);
  return li;
}

/**
 * Creates an inline delete confirmation row.
 * @param {Object} task - Task object
 * @returns {HTMLLIElement} List item with confirmation buttons
 */
function createDeleteConfirmElement(task) {
  const li = document.createElement('li');
  li.className = 'task-item';
  li.dataset.id = task.id;

  const box = document.createElement('div');
  box.className = 'delete-confirm-box';

  const promptSpan = document.createElement('span');
  promptSpan.className = 'delete-prompt';
  promptSpan.textContent = 'Delete this task permanently?';

  const actions = document.createElement('div');
  actions.className = 'delete-actions';

  const yesBtn = document.createElement('button');
  yesBtn.type = 'button';
  yesBtn.className = 'btn-confirm-delete';
  yesBtn.dataset.action = 'confirm-delete';
  yesBtn.dataset.id = task.id;
  yesBtn.textContent = 'Yes, Delete';
  yesBtn.setAttribute('aria-label', `Confirm deletion of task: ${task.text}`);

  const noBtn = document.createElement('button');
  noBtn.type = 'button';
  noBtn.className = 'btn-cancel-delete';
  noBtn.dataset.action = 'cancel-delete';
  noBtn.dataset.id = task.id;
  noBtn.textContent = 'Cancel';
  noBtn.setAttribute('aria-label', 'Cancel deletion');

  actions.appendChild(yesBtn);
  actions.appendChild(noBtn);

  box.appendChild(promptSpan);
  box.appendChild(actions);

  li.appendChild(box);
  return li;
}

/**
 * Creates a standard task row.
 * Uses textContent and createElement exclusively — zero XSS risk.
 * @param {Object} task - Task object
 * @returns {HTMLLIElement} Rendered list item
 */
function createTaskItemElement(task) {
  const li = document.createElement('li');
  li.className = 'task-item' + (task.completed ? ' task-item-completed' : '');
  li.dataset.id = task.id;

  // Toggle button / Checkbox
  const toggleBtn = document.createElement('button');
  toggleBtn.type = 'button';
  toggleBtn.className = 'task-toggle-btn' + (task.completed ? ' is-checked' : '');
  toggleBtn.setAttribute('role', 'checkbox');
  toggleBtn.setAttribute('aria-checked', task.completed ? 'true' : 'false');
  toggleBtn.setAttribute('aria-label', task.completed ? `Mark "${task.text}" as pending` : `Mark "${task.text}" as completed`);
  toggleBtn.dataset.action = 'toggle';
  toggleBtn.dataset.id = task.id;

  if (task.completed) {
    const checkSvg = createSvgElement('check');
    checkSvg.classList.add('task-toggle-icon');
    toggleBtn.appendChild(checkSvg);
  }

  // Middle content container
  const contentDiv = document.createElement('div');
  contentDiv.className = 'task-content';

  const textSpan = document.createElement('span');
  textSpan.className = 'task-text';
  textSpan.textContent = task.text; // Safe text insertion
  contentDiv.appendChild(textSpan);

  // Timestamps container
  const timestampsDiv = document.createElement('div');
  timestampsDiv.className = 'task-timestamps';

  // Created timestamp
  const createdSpan = document.createElement('span');
  createdSpan.className = 'timestamp';
  const clockSvg = createSvgElement('clock');
  clockSvg.classList.add('timestamp-icon');
  createdSpan.appendChild(clockSvg);
  const createdText = document.createTextNode(` Added: ${formatTimestamp(task.createdAt)}`);
  createdSpan.appendChild(createdText);
  timestampsDiv.appendChild(createdSpan);

  // Completed timestamp (bonus)
  if (task.completed && task.completedAt) {
    const completedSpan = document.createElement('span');
    completedSpan.className = 'timestamp completed-timestamp';
    const checkCircleSvg = createSvgElement('check-circle');
    checkCircleSvg.classList.add('timestamp-icon');
    completedSpan.appendChild(checkCircleSvg);
    const completedText = document.createTextNode(` Completed: ${formatTimestamp(task.completedAt)}`);
    completedSpan.appendChild(completedText);
    timestampsDiv.appendChild(completedSpan);
  }

  contentDiv.appendChild(timestampsDiv);

  // Action buttons cluster
  const actionsDiv = document.createElement('div');
  actionsDiv.className = 'task-actions';

  // Edit Button
  const editBtn = document.createElement('button');
  editBtn.type = 'button';
  editBtn.className = 'btn-icon btn-edit';
  editBtn.dataset.action = 'edit';
  editBtn.dataset.id = task.id;
  editBtn.setAttribute('aria-label', `Edit task: ${task.text}`);
  const editSvg = createSvgElement('edit');
  editBtn.appendChild(editSvg);

  // Delete Button
  const deleteBtn = document.createElement('button');
  deleteBtn.type = 'button';
  deleteBtn.className = 'btn-icon btn-delete';
  deleteBtn.dataset.action = 'delete';
  deleteBtn.dataset.id = task.id;
  deleteBtn.setAttribute('aria-label', `Delete task: ${task.text}`);
  const deleteSvg = createSvgElement('trash');
  deleteBtn.appendChild(deleteSvg);

  actionsDiv.appendChild(editBtn);
  actionsDiv.appendChild(deleteBtn);

  // Assemble full task item
  li.appendChild(toggleBtn);
  li.appendChild(contentDiv);
  li.appendChild(actionsDiv);

  return li;
}

/**
 * Re-renders the entire UI from the single source of truth (tasks array).
 */
function render() {
  const pendingTasks = tasks.filter(t => !t.completed);
  const completedTasks = tasks.filter(t => t.completed);

  // Update counters
  elements.pendingCount.textContent = `${pendingTasks.length} pending`;
  elements.completedCount.textContent = `${completedTasks.length} completed`;

  // Update Clear Completed button visibility
  if (completedTasks.length > 0) {
    elements.clearCompletedBtn.hidden = false;
  } else {
    elements.clearCompletedBtn.hidden = true;
  }

  // Update Progress Bar
  const totalTasks = tasks.length;
  const percent = totalTasks === 0 ? 0 : Math.round((completedTasks.length / totalTasks) * 100);
  elements.progressBar.setAttribute('aria-valuenow', percent);
  elements.progressFill.style.width = `${percent}%`;
  elements.progressPercent.textContent = `${percent}%`;

  // Render Pending List
  elements.pendingList.replaceChildren();
  if (pendingTasks.length === 0) {
    elements.pendingList.appendChild(
      createEmptyStateItem('Nothing pending. Enjoy the calm! 🎉', '☕')
    );
  } else {
    pendingTasks.forEach(task => {
      if (task.id === editingTaskId) {
        elements.pendingList.appendChild(createEditItemElement(task));
      } else if (task.id === deletingTaskId) {
        elements.pendingList.appendChild(createDeleteConfirmElement(task));
      } else {
        elements.pendingList.appendChild(createTaskItemElement(task));
      }
    });
  }

  // Render Completed List
  elements.completedList.replaceChildren();
  if (completedTasks.length === 0) {
    elements.completedList.appendChild(
      createEmptyStateItem('No completed tasks yet. Finish one to see it here.', '✨')
    );
  } else {
    completedTasks.forEach(task => {
      if (task.id === editingTaskId) {
        elements.completedList.appendChild(createEditItemElement(task));
      } else if (task.id === deletingTaskId) {
        elements.completedList.appendChild(createDeleteConfirmElement(task));
      } else {
        elements.completedList.appendChild(createTaskItemElement(task));
      }
    });
  }
}

// ============================================================================
// 9. Delegated Event Handlers
// ============================================================================

/**
 * Event delegation handler for click actions on a task list.
 * @param {MouseEvent} event
 */
function handleTaskListClick(event) {
  const actionBtn = event.target.closest('[data-action]');
  if (!actionBtn) return;

  const action = actionBtn.dataset.action;
  const id = actionBtn.dataset.id;
  if (!id) return;

  switch (action) {
    case 'toggle':
      toggleTask(id);
      break;

    case 'edit':
      enterEditMode(id);
      break;

    case 'cancel':
      cancelEditMode();
      break;

    case 'save': {
      const editInput = document.querySelector(`.task-edit-input[data-id="${id}"]`);
      if (editInput) {
        saveEditTask(id, editInput.value);
      }
      break;
    }

    case 'delete':
      promptDeleteTask(id);
      break;

    case 'cancel-delete':
      cancelDeleteTask();
      break;

    case 'confirm-delete':
      confirmDeleteTask(id);
      break;

    default:
      break;
  }
}

/**
 * Event delegation handler for keyboard interactions inside a task list (Enter/Escape in edit mode).
 * @param {KeyboardEvent} event
 */
function handleTaskListKeydown(event) {
  const editInput = event.target.closest('.task-edit-input');
  if (!editInput) return;

  const id = editInput.dataset.id;

  if (event.key === 'Enter') {
    event.preventDefault();
    saveEditTask(id, editInput.value);
  } else if (event.key === 'Escape') {
    event.preventDefault();
    cancelEditMode();
  }
}

// ============================================================================
// 10. Initialization & Global Listeners
// ============================================================================

function init() {
  // Load tasks from storage
  tasks = loadTasks();

  // Initial render
  render();

  // Autofocus the main input field on page load
  elements.taskInput.focus();
  updateCharCounter();

  // Form Submission
  elements.taskForm.addEventListener('submit', event => {
    event.preventDefault();
    addTask(elements.taskInput.value);
  });

  // Character counter & clear error on input
  elements.taskInput.addEventListener('input', () => {
    updateCharCounter();
    if (elements.inputError.textContent.trim()) {
      clearInputError();
    }
  });

  // Event Delegation for both task lists
  elements.pendingList.addEventListener('click', handleTaskListClick);
  elements.pendingList.addEventListener('keydown', handleTaskListKeydown);

  elements.completedList.addEventListener('click', handleTaskListClick);
  elements.completedList.addEventListener('keydown', handleTaskListKeydown);

  // Clear completed tasks button
  elements.clearCompletedBtn.addEventListener('click', clearCompletedTasks);
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', init);
} else {
  init();
}
