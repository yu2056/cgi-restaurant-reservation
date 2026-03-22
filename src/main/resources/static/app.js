const planEl = document.getElementById('plan');
const messageEl = document.getElementById('message');
const reservationsListEl = document.getElementById('reservations-list');
const menuSuggestionEl = document.getElementById('menu-suggestion');

const searchBtn = document.getElementById('search-btn');
const bookBtn = document.getElementById('book-btn');
const refreshBtn = document.getElementById('refresh-btn');
const guestTab = document.getElementById('guest-tab');
const adminTab = document.getElementById('admin-tab');

let lastPlan = null;
let selectedTableIds = [];
let adminMode = false;
let dragging = null;
let dragOffset = { x: 0, y: 0 };

function setTodayDefaults() {
    const now = new Date();
    const date = document.getElementById('date');
    const time = document.getElementById('time');
    date.value = now.toISOString().slice(0, 10);
    const rounded = new Date(now);
    rounded.setMinutes(Math.ceil(rounded.getMinutes() / 15) * 15, 0, 0);
    time.value = rounded.toTimeString().slice(0, 5);
}

function getPreferences() {
    return [...document.querySelectorAll('.prefs input:checked')].map(el => el.value);
}

function getQuery() {
    const params = new URLSearchParams();
    params.set('date', document.getElementById('date').value);
    params.set('time', document.getElementById('time').value);
    params.set('partySize', document.getElementById('partySize').value);
    params.set('durationMinutes', document.getElementById('durationMinutes').value);
    const zone = document.getElementById('zone').value;
    if (zone) params.set('zone', zone);
    for (const pref of getPreferences()) params.append('preferences', pref);
    return params;
}

function setMessage(text) {
    messageEl.textContent = text;
}

async function loadPlan() {
    const query = getQuery();
    const res = await fetch(`/api/plan?${query.toString()}`);
    if (!res.ok) throw new Error('Failed to load plan');
    lastPlan = await res.json();
    renderPlan(lastPlan);
    renderReservations(lastPlan.reservations || []);
    renderMenu(lastPlan.menuSuggestion);
    updateSelectionFromRecommendations(lastPlan.recommendations || []);
}

function updateSelectionFromRecommendations(recommendations) {
    if (!recommendations.length) {
        selectedTableIds = [];
        return;
    }
    selectedTableIds = recommendations[0].tableIds;
}

function renderMenu(menu) {
    if (!menu) {
        menuSuggestionEl.textContent = 'No menu suggestion available right now.';
        return;
    }
    menuSuggestionEl.innerHTML = `
        <strong>${menu.title}</strong><br/>
        <span>${menu.category || 'Chef suggestion'}</span><br/>
        <span>${menu.note || ''}</span>
        ${menu.sourceUrl ? `<br/><a href="${menu.sourceUrl}" target="_blank" rel="noreferrer">Open source</a>` : ''}
    `;
}

function renderReservations(reservations) {
    reservationsListEl.innerHTML = '';
    if (!reservations.length) {
        reservationsListEl.innerHTML = '<div class="reservation-item">No reservations in the selected time window.</div>';
        return;
    }
    for (const r of reservations) {
        const item = document.createElement('div');
        item.className = 'reservation-item';
        item.innerHTML = `
            <strong>${r.customerName || 'Guest'}</strong> · ${r.start.substring(11, 16)}–${r.end.substring(11, 16)}<br/>
            Tables: ${r.tableIds.join(', ')}<br/>
            Party: ${r.partySize}
        `;
        reservationsListEl.appendChild(item);
    }
}

function renderPlan(plan) {
    planEl.innerHTML = '';
    selectedTableIds = (plan.recommendations?.[0]?.tableIds) || [];

    const zoneLabels = [
        { text: 'Dining hall', x: 20, y: 20 },
        { text: 'Terrace', x: 650, y: 40 },
        { text: 'Private room', x: 640, y: 470 }
    ];
    for (const z of zoneLabels) {
        const el = document.createElement('div');
        el.className = 'zone-label';
        el.style.left = `${z.x}px`;
        el.style.top = `${z.y}px`;
        el.textContent = z.text;
        planEl.appendChild(el);
    }

    for (const table of plan.tables || []) {
        const el = document.createElement('div');
        el.className = `table ${table.occupied ? 'occupied' : 'free'} ${table.recommended ? 'recommended' : ''} ${table.combined ? 'combined' : ''} ${adminMode ? 'admin-mode' : ''}`;
        el.style.left = `${table.x}px`;
        el.style.top = `${table.y}px`;
        el.style.width = `${table.width}px`;
        el.style.height = `${table.height}px`;
        el.dataset.id = table.id;

        const featureText = (table.features || []).join(', ');
        const reason = table.reason ? `<div class="meta">${table.reason}</div>` : '';
        el.innerHTML = `
            <div class="label">${table.label}</div>
            <div class="meta">
                <span>${table.seats} seats</span>
                <span>${table.zone}</span>
            </div>
            <div class="meta">${featureText || '&nbsp;'}</div>
            ${reason}
        `;

        if (adminMode) {
            el.setAttribute('draggable', 'true');
            el.addEventListener('dragstart', onDragStart);
            el.addEventListener('dragend', onDragEnd);
        }

        el.addEventListener('click', () => {
            if (table.recommended) {
                selectedTableIds = table.tableIds || [table.id];
                setMessage(`Selected recommendation: ${selectedTableIds.join(', ')}`);
            }
        });

        planEl.appendChild(el);
    }
}

function onDragStart(e) {
    dragging = e.currentTarget;
    const rect = dragging.getBoundingClientRect();
    const planRect = planEl.getBoundingClientRect();
    dragOffset = {
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
    };
    dragging.classList.add('dragging');
    e.dataTransfer.setData('text/plain', dragging.dataset.id);
    e.dataTransfer.effectAllowed = 'move';
}

function onDragEnd(e) {
    if (dragging) dragging.classList.remove('dragging');
    dragging = null;
}

planEl.addEventListener('dragover', (e) => {
    if (!adminMode || !dragging) return;
    e.preventDefault();
    const planRect = planEl.getBoundingClientRect();
    const x = Math.max(0, Math.min(e.clientX - planRect.left - dragOffset.x, planEl.clientWidth - dragging.offsetWidth));
    const y = Math.max(0, Math.min(e.clientY - planRect.top - dragOffset.y, planEl.clientHeight - dragging.offsetHeight));
    dragging.style.left = `${x}px`;
    dragging.style.top = `${y}px`;
});

planEl.addEventListener('drop', async (e) => {
    if (!adminMode || !dragging) return;
    e.preventDefault();
    const id = dragging.dataset.id;
    const x = parseInt(dragging.style.left, 10);
    const y = parseInt(dragging.style.top, 10);
    try {
        const res = await fetch(`/api/admin/tables/${id}/position`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ x, y })
        });
        if (!res.ok) throw new Error('save failed');
        setMessage(`Saved new position for ${id}`);
        await loadPlan();
    } catch (err) {
        setMessage('Could not save new table position.');
    }
});

async function bookBest() {
    const query = getQuery();
    const planRes = await fetch(`/api/plan?${query.toString()}`);
    const plan = await planRes.json();
    const best = plan.recommendations?.[0];
    if (!best) {
        setMessage('No available table matched the filters.');
        return;
    }

    const payload = {
        customerName: document.getElementById('customerName').value || 'Guest',
        date: document.getElementById('date').value,
        time: document.getElementById('time').value,
        durationMinutes: Number(document.getElementById('durationMinutes').value),
        partySize: Number(document.getElementById('partySize').value),
        zone: document.getElementById('zone').value || null,
        preferences: getPreferences(),
        tableIds: best.tableIds
    };

    const res = await fetch('/api/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });

    const data = await res.json();
    if (!res.ok) {
        setMessage(data.message || 'Reservation failed.');
        return;
    }

    setMessage(`Reservation created: ${data.reservationId}. Table(s): ${data.tableIds.join(', ')}`);
    await loadPlan();
}

searchBtn.addEventListener('click', async (e) => {
    e.preventDefault();
    try {
        await loadPlan();
        setMessage('Best recommendation loaded.');
    } catch (err) {
        setMessage('Could not load plan.');
    }
});

bookBtn.addEventListener('click', async (e) => {
    e.preventDefault();
    try {
        await bookBest();
    } catch (err) {
        setMessage('Could not create reservation.');
    }
});

refreshBtn.addEventListener('click', async (e) => {
    e.preventDefault();
    try {
        await loadPlan();
        setMessage('Plan refreshed.');
    } catch (err) {
        setMessage('Could not refresh plan.');
    }
});

guestTab.addEventListener('click', () => {
    adminMode = false;
    guestTab.classList.add('active');
    adminTab.classList.remove('active');
    loadPlan();
});

adminTab.addEventListener('click', () => {
    adminMode = true;
    adminTab.classList.add('active');
    guestTab.classList.remove('active');
    loadPlan();
});

setTodayDefaults();
loadPlan().catch(() => setMessage('Failed to initialize the floor plan.'));
