/* Header rendering script
   Dynamically builds the header based on current user role and session.
*/

function tryImportOpenModal(type) {
  if (typeof openModal === 'function') {
    try { openModal(type); } catch (e) { console.error(e); }
    return;
  }
  // dynamic import as fallback
  import('/js/components/modals.js')
    .then(mod => {
      if (mod && typeof mod.openModal === 'function') mod.openModal(type);
    })
    .catch(err => console.error('Failed to load modals module', err));
}

function renderHeader() {
  const headerDiv = document.getElementById('header');
  if (!headerDiv) return;

  const pathname = window.location.pathname || '/';
  // if on root, clear role and show minimal header
  if (pathname === '/' || pathname.endsWith('/')) {
    localStorage.removeItem('userRole');
    headerDiv.innerHTML = `
      <header class="header">
        <div class="logo-section">
          <img src="/assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
          <span class="logo-title">Hospital CMS</span>
        </div>
      </header>`;
    return;
  }

  const role = localStorage.getItem('userRole');
  const token = localStorage.getItem('token');

  // session check
  if ((role === 'loggedPatient' || role === 'admin' || role === 'doctor') && !token) {
    localStorage.removeItem('userRole');
    alert('Session expired or invalid login. Please log in again.');
    window.location.href = '/';
    return;
  }

  let headerContent = `
    <header class="header">
      <div class="logo-section">
        <img src="/assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
        <span class="logo-title">Hospital CMS</span>
      </div>
      <nav class="header-nav">`;

  if (role === 'admin') {
    headerContent += `
        <button id="addDocBtn" class="adminBtn">Add Doctor</button>
        <button id="logoutBtn" class="adminBtn">Logout</button>`;
  } else if (role === 'doctor') {
    headerContent += `
        <button id="doctorHome" class="adminBtn">Home</button>
        <button id="logoutBtn" class="adminBtn">Logout</button>`;
  } else if (role === 'patient') {
    headerContent += `
        <button id="patientLogin" class="adminBtn">Login</button>
        <button id="patientSignup" class="adminBtn">Sign Up</button>`;
  } else if (role === 'loggedPatient') {
    headerContent += `
        <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
        <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
        <button id="logoutPatientBtn" class="adminBtn">Logout</button>`;
  } else {
    // default when no role set: show role selection hints
    headerContent += `
        <a href="/" class="adminBtn">Select Role</a>`;
  }

  headerContent += `
      </nav>
    </header>`;

  headerDiv.innerHTML = headerContent;
  attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
  const addDocBtn = document.getElementById('addDocBtn');
  const patientLogin = document.getElementById('patientLogin');
  const patientSignup = document.getElementById('patientSignup');
  const logoutBtn = document.getElementById('logoutBtn');
  const logoutPatientBtn = document.getElementById('logoutPatientBtn');
  const doctorHome = document.getElementById('doctorHome');

  if (addDocBtn) addDocBtn.addEventListener('click', () => tryImportOpenModal('addDoctor'));
  if (patientLogin) patientLogin.addEventListener('click', () => tryImportOpenModal('patientLogin'));
  if (patientSignup) patientSignup.addEventListener('click', () => tryImportOpenModal('patientSignup'));
  if (logoutBtn) logoutBtn.addEventListener('click', logout);
  if (logoutPatientBtn) logoutPatientBtn.addEventListener('click', logoutPatient);
  if (doctorHome) doctorHome.addEventListener('click', () => {
    if (typeof selectRole === 'function') selectRole('doctor');
    else window.location.href = '/';
  });
}

function logout() {
  localStorage.removeItem('userRole');
  localStorage.removeItem('token');
  window.location.href = '/';
}

function logoutPatient() {
  localStorage.removeItem('userRole');
  localStorage.removeItem('token');
  window.location.href = '/pages/patientDashboard.html';
}

// Expose renderHeader globally and run once to populate header on load
window.renderHeader = renderHeader;

document.addEventListener('DOMContentLoaded', renderHeader);

