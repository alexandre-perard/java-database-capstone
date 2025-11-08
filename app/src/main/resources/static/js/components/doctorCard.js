// doctorCard.js
// Renders a doctor card element with role-based actions (admin delete, patient booking).

import { showBookingOverlay } from '../loggedPatient.js';
import { deleteDoctor } from '../services/doctorServices.js';
import { getPatientData } from '../services/patientServices.js';
import { openModal } from './modals.js';

export function createDoctorCard(doctor) {
  // main card container
  const card = document.createElement('div');
  card.className = 'doctor-card';
  card.dataset.doctorId = doctor.id;

  // doctor info
  const info = document.createElement('div');
  info.className = 'doctor-info';

  const name = document.createElement('h3');
  name.textContent = doctor.name || 'Unknown Doctor';

  const specialty = document.createElement('p');
  specialty.className = 'doctor-specialty';
  specialty.textContent = doctor.specialty || '';

  const email = document.createElement('p');
  email.className = 'doctor-email';
  email.textContent = doctor.email || '';

  // available times
  const timesContainer = document.createElement('div');
  timesContainer.className = 'doctor-times';

  if (Array.isArray(doctor.availableTimes) && doctor.availableTimes.length > 0) {
    doctor.availableTimes.forEach(t => {
      const span = document.createElement('span');
      span.className = 'time-slot';
      span.textContent = t;
      timesContainer.appendChild(span);
    });
  } else {
    const noTimes = document.createElement('span');
    noTimes.className = 'no-times';
    noTimes.textContent = 'No available times';
    timesContainer.appendChild(noTimes);
  }

  info.appendChild(name);
  info.appendChild(specialty);
  info.appendChild(email);
  info.appendChild(timesContainer);

  // actions container
  const actions = document.createElement('div');
  actions.className = 'doctor-actions';

  const userRole = localStorage.getItem('userRole');
  const token = localStorage.getItem('token');

  // ADMIN actions: delete doctor
  if (userRole === 'admin') {
    const delBtn = document.createElement('button');
    delBtn.className = 'delete-doctor-btn';
    delBtn.textContent = 'Delete';
    delBtn.addEventListener('click', async () => {
      if (!confirm(`Delete Dr. ${doctor.name}? This action cannot be undone.`)) return;
      const adminToken = token;
      if (!adminToken) {
        alert('Admin token missing. Please log in as admin.');
        return;
      }
      try {
        const res = await deleteDoctor(doctor.id, adminToken);
        if (res && res.success) {
          alert(res.message || 'Doctor deleted');
          card.remove();
        } else {
          alert('Failed to delete doctor: ' + (res.message || 'Unknown error'));
        }
      } catch (err) {
        console.error('Error deleting doctor:', err);
        alert('An error occurred while deleting the doctor.');
      }
    });
    actions.appendChild(delBtn);
  }

  // PATIENT actions
  const bookBtn = document.createElement('button');
  bookBtn.className = 'book-now-btn';
  bookBtn.textContent = 'Book Now';

  if (!userRole || (userRole === 'patient' && !token)) {
    // not logged in patient: prompt login or signup
    bookBtn.addEventListener('click', () => {
      alert('Please log in or sign up to book an appointment.');
      // open login modal if available
      try { openModal('patientLogin'); } catch (e) { /* ignore if modal not present */ }
    });
    actions.appendChild(bookBtn);
  } else if (userRole === 'loggedPatient') {
    // logged-in patient: fetch patient data and show booking overlay
    bookBtn.addEventListener('click', async (e) => {
      const storedToken = token;
      if (!storedToken) {
        // token missing, redirect to login
        try { openModal('patientLogin'); } catch (ex) { window.location.href = '/'; }
        return;
      }

      const patient = await getPatientData(storedToken);
      if (!patient) {
        alert('Failed to retrieve patient details. Please log in again.');
        return;
      }

      // show booking overlay from loggedPatient.js
      try {
        showBookingOverlay(e, doctor, patient);
      } catch (err) {
        console.error('Error showing booking overlay:', err);
        alert('Could not open booking overlay.');
      }
    });
    actions.appendChild(bookBtn);
  } else {
    // other roles (e.g., doctor) get a disabled book button
    bookBtn.disabled = true;
    actions.appendChild(bookBtn);
  }

  // assemble
  card.appendChild(info);
  card.appendChild(actions);

  return card;
}

