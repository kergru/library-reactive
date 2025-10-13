document.addEventListener('DOMContentLoaded', () => {
  const card = document.querySelector('.book-details');
  if (!card) return;

  const isbn = card.getAttribute('data-isbn');
  const borrowButton = document.getElementById('borrow-button');
  const alertContainer = document.getElementById('loan-alert-container');
  const badgeAvailable = document.getElementById('badge-available');
  const badgeUnavailable = document.getElementById('badge-unavailable');

  const getCookie = (name) => {
    const m = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
    return m ? decodeURIComponent(m[1]) : null;
  };

  const resolveCsrf = () => {
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    const token  = document.querySelector('meta[name="_csrf"]')?.content;
    return { headerName: header, token: token };
  };

  const setAlert = (html) => {
    if (alertContainer) {
      alertContainer.innerHTML = html;
    }
  };

  if (!borrowButton) return;

  let busy = false;
  borrowButton.addEventListener('click', async () => {
    if (busy) return;
    busy = true;

    setAlert('');
    const originalText = borrowButton.textContent;
    borrowButton.disabled = true;
    borrowButton.classList.add('disabled');
    borrowButton.textContent = 'Wird ausgeliehen…';

    const csrf = resolveCsrf()
    console.log( csrf.headerName + "- " + csrf.token);
    try {
      const response = await fetch(`/library/ui/me/borrowBook/${encodeURIComponent(isbn)}`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: csrf ? { [csrf.headerName]: csrf.token } : {}
      });

      if (response.ok) {
        setAlert(`
          <div class="alert alert-success alert-dismissible fade show" role="alert">
            Buch wurde erfolgreich ausgeliehen.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
          </div>
        `);

        // Badge umschalten
        if (badgeAvailable) badgeAvailable.style.display = 'none';
        if (badgeUnavailable) {
          badgeUnavailable.style.display = 'inline';
        } else if (badgeAvailable && badgeAvailable.parentNode) {
          const newBadge = document.createElement('span');
          newBadge.id = 'badge-unavailable';
          newBadge.className = 'badge bg-danger';
          newBadge.textContent = 'Ausgeliehen';
          badgeAvailable.parentNode.appendChild(newBadge);
        }

        // Button dauerhaft deaktivieren
        borrowButton.classList.remove('btn-primary');
        borrowButton.classList.add('btn-secondary');
        borrowButton.textContent = 'Nicht verfügbar';
      } else if (response.status === 409) {
        setAlert(`
          <div class="alert alert-danger alert-dismissible fade show" role="alert">
            Dieses Buch ist bereits ausgeliehen.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
          </div>
        `);
        // ursprünglichen Button-Zustand zurück
        borrowButton.disabled = false;
        borrowButton.classList.remove('disabled');
        borrowButton.textContent = originalText;
      } else if (response.status === 401) {
        // Nicht eingeloggt → optional auf Login umleiten
        setAlert(`
          <div class="alert alert-warning alert-dismissible fade show" role="alert">
            Bitte anmelden, um Bücher auszuleihen.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
          </div>
        `);
        borrowButton.disabled = false;
        borrowButton.classList.remove('disabled');
        borrowButton.textContent = originalText;
      } else {
        setAlert(`
          <div class="alert alert-warning alert-dismissible fade show" role="alert">
            Ein unbekannter Fehler ist aufgetreten.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
          </div>
        `);
        borrowButton.disabled = false;
        borrowButton.classList.remove('disabled');
        borrowButton.textContent = originalText;
      }
    } catch (e) {
      console.error(e);
      setAlert(`
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
          Fehler beim Verbinden mit dem Server.
          <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
      `);
      borrowButton.disabled = false;
      borrowButton.classList.remove('disabled');
      borrowButton.textContent = originalText;
    } finally {
      busy = false;
    }
  });
});
