/**
 * HMS – App Utilities (toast, modal, session helpers)
 */

// ─── Toast ────────────────────────────────────────────────────
function toast(message, type = 'info') {
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  const container = document.getElementById('toast-container')
    || (() => {
      const el = document.createElement('div');
      el.id = 'toast-container';
      document.body.appendChild(el);
      return el;
    })();

  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `<span>${icons[type] || ''}</span><span>${message}</span>`;
  container.appendChild(el);
  setTimeout(() => { el.style.opacity = '0'; el.style.transform = 'translateX(100%)';
    el.style.transition = 'all .3s'; setTimeout(() => el.remove(), 300); }, 3000);
}

// ─── Modal ────────────────────────────────────────────────────
function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }
function closeAllModals() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('open'));
}

// Close modal on overlay click
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-overlay')) closeAllModals();
});

// ─── Session helpers ──────────────────────────────────────────
function getSession() {
  try { return JSON.parse(sessionStorage.getItem('hmsUser') || 'null'); } catch { return null; }
}
function setSession(user) { sessionStorage.setItem('hmsUser', JSON.stringify(user)); }
function clearSession()   { sessionStorage.removeItem('hmsUser'); }

// ─── Auth guard ───────────────────────────────────────────────
async function requireAuth() {
  const res = await API.auth.me();
  if (!res.success) { window.location.href = '/hms/static/html/login.html'; return null; }
  setSession(res.data);
  return res.data;
}

// ─── Render sidebar active state ──────────────────────────────
function initSidebar(activeId) {
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.page === activeId);
  });
  const user = getSession();
  if (user) {
    const el = document.getElementById('sidebar-username');
    const roleEl = document.getElementById('sidebar-role');
    const avatarEl = document.getElementById('sidebar-avatar');
    if (el)     el.textContent = user.username;
    if (roleEl) roleEl.textContent = user.role;
    if (avatarEl) avatarEl.textContent = (user.username || 'U')[0].toUpperCase();

    // Role-based UI logic
    if (user.role === 'PATIENT') {
      document.querySelectorAll('[data-page="patients"], [data-page="users"], [data-page="billing"], [data-role="ADMIN"], a[href="patients.html"], a[href="users.html"], a[href="billing.html"], .admin-only, .doctor-only').forEach(el => { if(el) el.style.display = 'none'; });
    } else if (user.role === 'DOCTOR') {
      document.querySelectorAll('[data-page="doctors"], [data-page="users"], [data-page="billing"], [data-role="ADMIN"], a[href="doctors.html"], a[href="users.html"], a[href="billing.html"], .admin-only, .patient-only').forEach(el => { if(el) el.style.display = 'none'; });
    } else if (user.role !== 'ADMIN') {
      document.querySelectorAll('[data-role="ADMIN"], .admin-only').forEach(el => { if(el) el.style.display = 'none'; });
    }
  }
}

// ─── Logout ───────────────────────────────────────────────────
async function logout() {
  await API.auth.logout();
  clearSession();
  window.location.href = '/hms/static/html/login.html';
}

// ─── Format helpers ───────────────────────────────────────────
function fmtDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}
function fmtCurrency(n) {
  return '₹ ' + parseFloat(n || 0).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}
function statusBadge(status) {
  const map = {
    CONFIRMED: 'badge-success', CANCELLED: 'badge-danger',
    PAID: 'badge-success',      UNPAID: 'badge-warning',
    ADMIN: 'badge-purple',      DOCTOR: 'badge-blue', PATIENT: 'badge-success',
  };
  return `<span class="badge ${map[status] || 'badge-blue'}">${status}</span>`;
}

// ─── Table empty state ────────────────────────────────────────
function emptyRow(cols, msg = 'No records found') {
  return `<tr><td colspan="${cols}" class="empty-state"><div class="empty-icon">📋</div><p>${msg}</p></td></tr>`;
}
// ─── Password Visibility Toggle ───────────────────────────────
function togglePassword(inputId, iconEl) {
  const input = document.getElementById(inputId);
  if (input.type === 'password') {
    input.type = 'text';
    iconEl.style.opacity = '1';
  } else {
    input.type = 'password';
    iconEl.style.opacity = '0.6';
  }
}
