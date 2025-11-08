/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form


  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')


  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors


  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them


  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()


  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert


  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area


  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/

import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';
import { openModal } from './components/modals.js';

const contentDiv = document.getElementById('content');
const searchBar = document.getElementById('searchBar');
const filterTime = document.getElementById('filterTime');
const filterSpecialty = document.getElementById('filterSpecialty');

async function loadDoctorCards() {
  if (!contentDiv) return;
  contentDiv.innerHTML = '';
  try {
    const doctors = await getDoctors();
    if (!doctors || doctors.length === 0) {
      contentDiv.innerHTML = '<p>No doctors available.</p>';
      return;
    }

    doctors.forEach(doc => {
      const card = createDoctorCard(doc);
      contentDiv.appendChild(card);
    });
  } catch (error) {
    console.error('Failed to load doctors:', error);
    contentDiv.innerHTML = '<p>Error loading doctors. Please try again later.</p>';
  }
}

function renderDoctorCards(doctors) {
  if (!contentDiv) return;
  contentDiv.innerHTML = '';
  doctors.forEach(doc => {
    const card = createDoctorCard(doc);
    contentDiv.appendChild(card);
  });
}

async function filterDoctorsOnChange() {
  const nameVal = searchBar ? searchBar.value.trim() : '';
  const timeVal = filterTime ? filterTime.value : '';
  const specVal = filterSpecialty ? filterSpecialty.value : '';

  const name = nameVal.length > 0 ? nameVal : null;
  const time = timeVal.length > 0 ? timeVal : null;
  const specialty = specVal.length > 0 ? specVal : null;

  try {
    const result = await filterDoctors(name, time, specialty);
    const doctors = result.doctors || result || [];
    if (doctors.length > 0) renderDoctorCards(doctors);
    else contentDiv.innerHTML = '<p>No doctors found with the given filters.</p>';
  } catch (error) {
    console.error('Failed to filter doctors:', error);
    alert('❌ An error occurred while filtering doctors.');
  }
}

// Admin add doctor handler called from modal save button
window.adminAddDoctor = async function () {
  try {
    const name = document.getElementById('doctorName')?.value?.trim() || '';
    const specialty = document.getElementById('specialization')?.value || '';
    const email = document.getElementById('doctorEmail')?.value?.trim() || '';
    const password = document.getElementById('doctorPassword')?.value || '';
    const phone = document.getElementById('doctorPhone')?.value?.trim() || '';

    const availabilityNodes = document.querySelectorAll('input[name="availability"]:checked');
    const availableTimes = Array.from(availabilityNodes).map(n => n.value);

    const token = localStorage.getItem('token');
    if (!token) {
      alert('You must be logged in as admin to add a doctor.');
      return;
    }

    const doctor = { name, specialty, email, password, phone, availableTimes };

    const { success, message } = await saveDoctor(doctor, token);
    if (success) {
      alert(message || 'Doctor added successfully');
      document.getElementById('modal').style.display = 'none';
      window.location.reload();
    } else {
      alert(message || 'Failed to save doctor');
    }
  } catch (error) {
    console.error('adminAddDoctor error:', error);
    alert('An error occurred while adding the doctor.');
  }
};

// wire filter inputs
if (searchBar) searchBar.addEventListener('input', filterDoctorsOnChange);
if (filterTime) filterTime.addEventListener('change', filterDoctorsOnChange);
if (filterSpecialty) filterSpecialty.addEventListener('change', filterDoctorsOnChange);

// Ensure we load cards on DOM ready
document.addEventListener('DOMContentLoaded', () => {
  if (typeof renderContent === 'function') renderContent();
  loadDoctorCards();
});

