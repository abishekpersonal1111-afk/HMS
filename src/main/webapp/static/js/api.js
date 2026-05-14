/**
 * HMS – API Helper (AJAX wrapper around fetch)
 * All calls return { success, message, data }
 */
const API = (() => {
  const BASE = '/hms/api';

  async function request(method, path, body = null) {
    const opts = {
      method,
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
    };
    if (body !== null) opts.body = JSON.stringify(body);
    try {
      const res = await fetch(BASE + path, opts);
      const json = await res.json();
      if (res.status === 401) {
        if (!window.location.pathname.includes('login.html')) {
          window.location.href = '/hms/static/html/login.html';
        }
        return { success: false, message: 'Unauthorized' };
      }
      return json;
    } catch (err) {
      return { success: false, message: 'Network error: ' + err.message };
    }
  }

  // Auth
  const auth = {
    login:    (u, p)     => request('POST', '/auth/login',    { username: u, password: p }),
    register: (u, p, r)  => request('POST', '/auth/register', { username: u, password: p, role: r }),
    logout:   ()         => request('POST', '/auth/logout'),
    me:       ()         => request('GET',  '/auth/me'),
  };

  // Patients
  const patients = {
    getAll:  (search = '') => request('GET',    `/patients${search ? '?search=' + encodeURIComponent(search) : ''}`),
    getById: id            => request('GET',    `/patients/${id}`),
    create:  data          => request('POST',   '/patients',    data),
    update:  (id, data)    => request('PUT',    `/patients/${id}`, data),
    delete:  id            => request('DELETE', `/patients/${id}`),
  };

  // Doctors
  const doctors = {
    getAll:  (spec = '') => request('GET',    `/doctors${spec ? '?specialization=' + encodeURIComponent(spec) : ''}`),
    getById: id          => request('GET',    `/doctors/${id}`),
    create:  data        => request('POST',   '/doctors',    data),
    update:  (id, data)  => request('PUT',    `/doctors/${id}`, data),
    delete:  id          => request('DELETE', `/doctors/${id}`),
  };

  // Appointments
  const appointments = {
    getAll:     (params = {}) => {
      const q = new URLSearchParams(params).toString();
      return request('GET', `/appointments${q ? '?' + q : ''}`);
    },
    getById:    id   => request('GET',    `/appointments/${id}`),
    schedule:   data => request('POST',   '/appointments', data),
    update:     (id, data) => request('PUT', `/appointments/${id}`, data),
    cancel:     id   => request('PATCH',  `/appointments/${id}/cancel`),
  };

  // Bills
  const bills = {
    getAll:    (patientId = null) => request('GET', `/bills${patientId ? '?patientId=' + patientId : ''}`),
    getById:   id   => request('GET',    `/bills/${id}`),
    generate:  data => request('POST',   '/bills', data),
    pay:       id   => request('PATCH',  `/bills/${id}/pay`),
    delete:    id   => request('DELETE', `/bills/${id}`),
  };

  // Prescriptions
  const prescriptions = {
    getAll:   (params = {}) => {
      const q = new URLSearchParams(params).toString();
      return request('GET', `/prescriptions${q ? '?' + q : ''}`);
    },
    getById:  id   => request('GET',    `/prescriptions/${id}`),
    create:   data => request('POST',   '/prescriptions', data),
    delete:   id   => request('DELETE', `/prescriptions/${id}`),
  };

  // Users
  const users = {
    getAll:  ()          => request('GET',    '/users'),
    getById: id          => request('GET',    `/users/${id}`),
    update:  (id, data)  => request('PUT',    `/users/${id}`, data),
    delete:  id          => request('DELETE', `/users/${id}`),
  };

  return { auth, patients, doctors, appointments, bills, prescriptions, users };
})();
