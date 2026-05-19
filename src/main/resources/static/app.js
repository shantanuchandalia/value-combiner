let currentType = "integer";
const calculationHistory = [];
const apiBaseUrl = window.VALUE_COMBINER_API_BASE_URL || "";

function openDrawer(status) {
    renderDrawer(status);
    document.getElementById("drawerOverlay").classList.add("open");
    document.getElementById("historyDrawer").classList.add("open");
    document.getElementById("historyDrawer").setAttribute("aria-hidden", "false");
}

function closeDrawer() {
    document.getElementById("drawerOverlay").classList.remove("open");
    document.getElementById("historyDrawer").classList.remove("open");
    document.getElementById("historyDrawer").setAttribute("aria-hidden", "true");
}

function renderDrawer(status) {
    const title = status === "success" ? "Successful Messages" : "Exception Messages";
    const rows = calculationHistory.filter(item => item.status === status);
    const drawerTitle = document.getElementById("drawerTitle");
    const drawerBody = document.getElementById("drawerBody");
    drawerTitle.textContent = title;

    if (rows.length === 0) {
        drawerBody.innerHTML = `<div class="empty-history">No ${status === "success" ? "successful" : "exception"} messages yet.</div>`;
        return;
    }

    drawerBody.innerHTML = `
        <table class="history-table">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Type</th>
                    <th>Input</th>
                    <th>Message</th>
                </tr>
            </thead>
            <tbody>
                ${rows.map((item, index) => `
                    <tr>
                        <td>${index + 1}</td>
                        <td>${escapeHtml(item.type)}</td>
                        <td>${escapeHtml(item.inputs.join(" + "))}</td>
                        <td class="history-message">${escapeHtml(item.message)}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

function escapeHtml(text) {
    const map = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#039;"
    };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}

document.addEventListener("keydown", event => {
    if (event.key === "Escape") {
        closeDrawer();
    }
});

function setType(type) {
    currentType = type;
    document.querySelectorAll(".type-btn").forEach(btn => {
        btn.classList.remove("active");
    });
    document.getElementById(`${type}TypeBtn`).classList.add("active");
    document.getElementById("resultContainer").classList.remove("show");
}

function addInput() {
    const container = document.getElementById("inputsContainer");
    const count = container.children.length;
    const index = count + 1;

    if (count < 4) {
        const group = document.createElement("div");
        group.id = `inputGroup${index}`;
        group.className = "input-group removable";
        group.innerHTML = `
            <input id="inputValue${index}" type="text" placeholder="Enter value" class="input-value">
            <button id="removeBtn${index}" class="remove-btn" onclick="removeInput(this)">&times;</button>
        `;
        container.appendChild(group);
        updateRemoveButtons();
        updateAddButtonState();
    }
}

function removeInput(btn) {
    const container = document.getElementById("inputsContainer");
    if (container.children.length > 2) {
        btn.parentElement.remove();
        updateRemoveButtons();
        updateAddButtonState();
    }
}

function updateRemoveButtons() {
    const container = document.getElementById("inputsContainer");
    const groups = container.querySelectorAll(".input-group");
    groups.forEach(group => {
        group.classList.toggle("removable", groups.length > 2);
    });
}

function updateAddButtonState() {
    const container = document.getElementById("inputsContainer");
    const addBtn = document.getElementById("addBtn");
    addBtn.disabled = container.children.length >= 4;
}

function resetInputs() {
    document.querySelectorAll(".input-value").forEach(input => {
        input.value = "";
    });
}

async function calculate() {
    const container = document.getElementById("inputsContainer");
    const inputs = Array.from(container.querySelectorAll(".input-value")).map(inp => inp.value);

    if (inputs.some(inp => inp.trim() === "")) {
        alert("Please fill all input fields");
        return;
    }

    document.getElementById("loading").classList.add("active");
    document.getElementById("resultContainer").classList.remove("show");

    try {
        const response = await fetch(`${apiBaseUrl}/api/calculate`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                inputs: inputs,
                dataType: currentType
            })
        });

        const data = await response.json();
        document.getElementById("loading").classList.remove("active");

        const resultContainer = document.getElementById("resultContainer");
        const resultValue = document.getElementById("resultValue");
        const resultLabel = document.getElementById("resultLabel");
        const passCount = document.getElementById("passCount");
        const exceptionCount = document.getElementById("exceptionCount");

        passCount.textContent = data.passCount;
        exceptionCount.textContent = data.exceptionCount;

        calculationHistory.push({
            status: data.status,
            type: currentType,
            inputs: inputs,
            message: `${data.status === "success" ? "PASS" : "FAIL"}: ${data.result}`
        });

        if (data.status === "success") {
            resultContainer.classList.remove("result-error");
            resultLabel.textContent = "Result";
            resultValue.textContent = data.result;
        } else {
            resultContainer.classList.add("result-error");
            resultLabel.textContent = "Error";
            resultValue.textContent = data.result;
        }

        resultContainer.classList.add("show");
        resetInputs();
    } catch (error) {
        document.getElementById("loading").classList.remove("active");
        alert(`Error: ${error.message}`);
    }
}

updateRemoveButtons();
updateAddButtonState();
