const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

async function api(path, options = {}) {
    const request = {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    };

    if (request.body && typeof request.body !== "string" && !(request.body instanceof FormData)) {
        request.body = JSON.stringify(request.body);
    }

    const response = await fetch(path, request);
    const rawText = await response.text();
    let payload = null;

    if (rawText) {
        try {
            payload = JSON.parse(rawText);
        } catch {
            payload = rawText;
        }
    }

    if (!response.ok) {
        const message = payload && payload.message ? payload.message : `请求失败 (${response.status})`;
        throw new Error(message);
    }

    return payload;
}

function setMessage(target, message, isError = false) {
    if (!target) {
        return;
    }
    target.textContent = message || "";
    target.classList.toggle("is-error", Boolean(isError));
}

function setButtonBusy(button, busy, labelWhenBusy) {
    if (!button) {
        return;
    }
    if (busy) {
        button.dataset.originalLabel = button.textContent;
        button.textContent = labelWhenBusy || "处理中...";
        button.disabled = true;
    } else {
        button.textContent = button.dataset.originalLabel || button.textContent;
        button.disabled = false;
    }
}

async function initAuthPage() {
    const current = await api("/api/auth/me");
    if (current) {
        window.location.replace("/app");
        return;
    }

    const page = document.body.dataset.page;
    const form = $("#authForm");
    const message = $("#authMessage");
    const submitButton = $("#authSubmit");

    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        setMessage(message, "");

        const payload = {
            username: $("#username").value.trim(),
            password: $("#password").value
        };

        const displayNameInput = $("#displayName");
        if (displayNameInput) {
            payload.displayName = displayNameInput.value.trim();
        }

        try {
            setButtonBusy(submitButton, true, page === "register" ? "正在创建..." : "正在登录...");
            await api(page === "register" ? "/api/auth/register" : "/api/auth/login", {
                method: "POST",
                body: payload
            });
            window.location.replace("/app");
        } catch (error) {
            setMessage(message, error.message || "操作失败", true);
        } finally {
            setButtonBusy(submitButton, false);
        }
    });
}

async function initAppPage() {
    const me = await api("/api/auth/me");
    if (!me) {
        window.location.replace("/login");
        return;
    }

    const userName = $("#userName");
    const userGreeting = $("#userGreeting");
    const userMeta = $("#userMeta");
    const historyCount = $("#historyCount");
    const totalCount = $("#totalCount");
    const recordCount = $("#recordCount");
    const generateForm = $("#generateForm");
    const generateButton = $("#generateButton");
    const refreshButton = $("#refreshButton");
    const logoutButton = $("#logoutButton");
    const statusMessage = $("#statusMessage");
    const historyList = $("#historyList");
    const outputBox = $("#outputBox");
    const archivePath = $("#archivePath");
    const recordTopic = $("#recordTopic");
    const recordTone = $("#recordTone");
    const recordCreatedAt = $("#recordCreatedAt");
    const recordDownload = $("#recordDownload");
    const recordCopy = $("#recordCopy");
    const recordPreview = $("#recordPreview");
    const recordDraft = $("#recordDraft");
    const recordNotes = $("#recordNotes");
    const recordEmpty = $("#recordEmpty");
    const recordLinks = $("#recordLinks");
    const note = $("#submitNote");

    userName.textContent = me.displayName;
    userGreeting.textContent = `欢迎，${me.displayName}`;
    userMeta.textContent = `@${me.username}`;

    let activeRecordId = null;
    let lastDetail = null;

    function renderDetail(record) {
        lastDetail = record;
        activeRecordId = record.id;
        outputBox.textContent = record.finalText || "";
        archivePath.textContent = record.archivePath || "暂未写入磁盘";
        recordTopic.textContent = record.topic || "未命名主题";
        recordTone.textContent = record.tone || "balanced";
        recordCreatedAt.textContent = record.createdAt || "-";
        recordPreview.textContent = record.finalText || "";
        recordDraft.textContent = record.draftText || "没有草稿内容";
        recordNotes.textContent = record.notes || "没有补充说明";
        recordDownload.href = record.downloadUrl || "#";
        recordDownload.classList.toggle("disabled", !record.downloadUrl);
        recordLinks.hidden = false;
        recordEmpty.hidden = true;
        $$(".history-item", historyList).forEach((item) => {
            item.classList.toggle("is-active", item.dataset.id === String(record.id));
        });
    }

    function renderHistory(items) {
        historyList.innerHTML = "";
        historyCount.textContent = String(items.length);

        if (!items.length) {
            const empty = document.createElement("div");
            empty.className = "history-empty";
            empty.textContent = "这里还没有生成记录。先写一个主题，点生成。";
            historyList.appendChild(empty);
            return;
        }

        items.forEach((item) => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "history-item";
            button.dataset.id = item.id;
            button.innerHTML = `
                <strong>${escapeHtml(item.topic || "未命名主题")}</strong>
                <span>${escapeHtml(item.createdAt || "-")} · ${escapeHtml(item.tone || "balanced")}</span>
                <p>${escapeHtml(item.preview || "")}</p>
            `;
            button.addEventListener("click", async () => {
                await openRecord(item.id);
            });
            historyList.appendChild(button);
        });
    }

    async function loadHistory(selectLatest = true) {
        const items = await api("/api/generations");
        renderHistory(items);
        if (selectLatest && items.length > 0) {
            await openRecord(items[0].id);
        }
    }

    async function openRecord(id) {
        const record = await api(`/api/generations/${id}`);
        renderDetail(record);
    }

    generateForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        setMessage(note, "");

        const payload = {
            topic: $("#topic").value.trim(),
            audience: $("#audience").value.trim(),
            tone: $("#tone").value,
            notes: $("#notes").value.trim()
        };

        try {
            setButtonBusy(generateButton, true, "正在生成...");
            const record = await api("/api/generations", {
                method: "POST",
                body: payload
            });
            renderDetail(record);
            setMessage(note, "已生成并落盘。", false);
            await loadHistory(false);
        } catch (error) {
            setMessage(note, error.message || "生成失败", true);
        } finally {
            setButtonBusy(generateButton, false);
        }
    });

    refreshButton.addEventListener("click", async () => {
        setMessage(statusMessage, "正在刷新历史记录...");
        try {
            await loadHistory();
            setMessage(statusMessage, "已刷新。");
        } catch (error) {
            setMessage(statusMessage, error.message || "刷新失败", true);
        }
    });

    logoutButton.addEventListener("click", async () => {
        try {
            await api("/api/auth/logout", { method: "POST" });
        } finally {
            window.location.replace("/login");
        }
    });

    recordCopy.addEventListener("click", async () => {
        if (!lastDetail || !lastDetail.finalText) {
            return;
        }
        await navigator.clipboard.writeText(lastDetail.finalText);
        setMessage(statusMessage, "已复制到剪贴板。");
    });

    await loadHistory();
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

document.addEventListener("DOMContentLoaded", () => {
    const page = document.body.dataset.page;
    if (page === "login" || page === "register") {
        initAuthPage();
    } else if (page === "app") {
        initAppPage();
    }
});
