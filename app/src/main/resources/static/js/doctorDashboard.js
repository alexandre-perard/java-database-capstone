/*
  Import getAllAppointments to fetch appointments from the backend
  Import createPatientRow to generate a table row for each patient appointment


  Get the table body where patient rows will be added
  Initialize selectedDate with today's date in 'YYYY-MM-DD' format
  Get the saved token from localStorage (used for authenticated API calls)
  Initialize patientName to null (used for filtering by name)


  Add an 'input' event listener to the search bar
  On each keystroke:
    - Trim and check the input value
    - If not empty, use it as the patientName for filtering
    - Else, reset patientName to "null" (as expected by backend)
    - Reload the appointments list with the updated filter


  Add a click listener to the "Today" button
  When clicked:
    - Set selectedDate to today's date
    - Update the date picker UI to match
    - Reload the appointments for today


  Add a change event listener to the date picker
  When the date changes:
    - Update selectedDate with the new value
    - Reload the appointments for that specific date


  Function: loadAppointments
  Purpose: Fetch and display appointments based on selected date and optional patient name

  Step 1: Call getAllAppointments with selectedDate, patientName, and token
  Step 2: Clear the table body content before rendering new rows

  Step 3: If no appointments are returned:
    - Display a message row: "No Appointments found for today."

  Step 4: If appointments exist:
    - Loop through each appointment and construct a 'patient' object with id, name, phone, and email
    - Call createPatientRow to generate a table row for the appointment
    - Append each row to the table body

  Step 5: Catch and handle any errors during fetch:
    - Show a message row: "Error loading appointments. Try again later."


  When the page is fully loaded (DOMContentLoaded):
    - Call renderContent() (assumes it sets up the UI layout)
    - Call loadAppointments() to display today's appointments by default
*/

import { getAllAppointments } from './services/appointmentRecordService.js';
import { createPatientRow } from './components/patientRows.js';

const tableBody = document.getElementById('patientTableBody');

// initialize selected date to today
let selectedDate = new Date().toISOString().slice(0, 10); // YYYY-MM-DD
const datePicker = document.getElementById('datePicker');
if (datePicker) datePicker.value = selectedDate;

// token may be present in the path (/doctorDashboard/{token}) or in localStorage
const pathParts = window.location.pathname.split('/').filter(Boolean);
let token = localStorage.getItem('token') || null;
if (!token && pathParts.length > 1) {
  // last segment may be token if served as /doctorDashboard/{token}
  token = pathParts[pathParts.length - 1];
}

let patientName = null; // filter by patient name

const searchBar = document.getElementById('searchBar');
if (searchBar) {
  searchBar.addEventListener('input', () => {
    const val = searchBar.value.trim();
    patientName = val.length > 0 ? val : 'null';
    loadAppointments();
  });
}

const todayBtn = document.getElementById('todayButton');
if (todayBtn) {
  todayBtn.addEventListener('click', () => {
    selectedDate = new Date().toISOString().slice(0, 10);
    if (datePicker) datePicker.value = selectedDate;
    loadAppointments();
  });
}

if (datePicker) {
  datePicker.addEventListener('change', () => {
    selectedDate = datePicker.value;
    loadAppointments();
  });
}

export async function loadAppointments() {
  if (!tableBody) return;
  tableBody.innerHTML = '';

  try {
    const resp = await getAllAppointments(selectedDate, patientName || 'null', token || 'null');

    // resp expected to be an object containing appointments or an array
    let appointments = [];
    if (Array.isArray(resp)) appointments = resp;
    else if (resp && resp.appointments) appointments = resp.appointments;
    else if (resp && resp.data) appointments = resp.data;

    if (!appointments || appointments.length === 0) {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td colspan="5" class="noPatientRecord">No Appointments found for the selected date.</td>`;
      tableBody.appendChild(tr);
      return;
    }

    appointments.forEach(app => {
      // appointment may include patient object or patient id; build a minimal patient object
      const patient = (app.patient && typeof app.patient === 'object') ? app.patient : {
        id: app.patient || 'N/A',
        name: app.patientName || 'Unknown',
        phone: app.patientPhone || '',
        email: app.patientEmail || ''
      };

      const appointmentId = app.id || app.appointmentId || null;
      const doctorId = (app.doctor && app.doctor.id) ? app.doctor.id : (app.doctorId || null);

      const row = createPatientRow(patient, appointmentId, doctorId);
      tableBody.appendChild(row);
    });
  } catch (error) {
    console.error('Error loading appointments:', error);
    const tr = document.createElement('tr');
    tr.innerHTML = `<td colspan="5" class="noPatientRecord">Error loading appointments. Try again later.</td>`;
    tableBody.appendChild(tr);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  if (typeof renderContent === 'function') renderContent();
  loadAppointments();
});

