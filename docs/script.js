/**
 * MINECRAFT MOB WIDGETS - FRONTEND APPLICATION SCRIPT
 * Single source of truth for app metadata, changelog, and interactions.
 */

const APP = {
  name: "Minecraft Mob Widgets",
  version: "1.0.0",
  versionCode: 1,
  releaseDate: "2026-10-12",
  apkUrl: "https://github.com/PrathamS1/MinecraftWidget/releases/download/v1.0.0/MinecraftWidget-v1.0.0.apk",
  apkSize: "26.5 MB",
  sha256: "975ed7d13f4a2d920d100af9a20e415ce1bdbee6ab9a36c58c60f5954a523c8e",
  discordUrl: "https://discord.gg/minecraft",
  githubUrl: "https://github.com/PrathamS1/MinecraftWidget",
  minAndroid: "Android 8.0+ (Oreo, API 26)",
  targetAndroid: "Android 14 (API 34)",
  mobCount: "108+",
  permissions: "Zero (No internet, zero telemetry, 100% offline)"
};

const CHANGELOG = [
  {
    version: "1.0.0",
    date: "2026-10-12",
    title: "Official Release: Living Companions on Your Home Screen",
    downloadUrl: "https://github.com/PrathamS1/MinecraftWidget/releases/download/v1.0.0/MinecraftWidget-v1.0.0.apk",
    changes: [
      { type: "new", text: "108 mob sprite variations bundled 100% offline with zero network latency." },
      { type: "new", text: "Real animated mob loops rendered with low-overhead bitmap slicing engine." },
      { type: "new", text: "Interactive tap sound effects featuring authentic in-game mob growls, purrs, and groans." },
      { type: "improved", text: "Clean light-mode settings UI with unified monochrome borders and stone accents." },
      { type: "improved", text: "Crisp carousel selector on home screen without blur or overlapping artifacts." },
      { type: "fixed", text: "Purged black padding rectangles from Ghast and Panda sprite animations." },
      { type: "improved", text: "Removed unnecessary internet permissions from AndroidManifest for total privacy." }
    ]
  },
  {
    version: "0.9.5",
    date: "2026-10-10",
    title: "Performance & Rendering Engine Refactor",
    changes: [
      { type: "improved", text: "Frame planner optimized to fit Android launcher memory budget without crashing." },
      { type: "improved", text: "Added high-density icon padding conforming to modern Android launcher standards." },
      { type: "fixed", text: "Corrected sprite aspect ratio scaling on ultra-wide home screen grids." }
    ]
  },
  {
    version: "0.9.0",
    date: "2026-10-08",
    title: "Beta Milestone: Comprehensive Mob Catalog",
    changes: [
      { type: "new", text: "Catalog browser indexing Allay, Creeper, Axolotl, Warden, Panda, and 20+ species." },
      { type: "new", text: "Custom widget configuration screen with sizing and companion styles." },
      { type: "improved", text: "Audio playback engine utilizing lightweight SoundPool with battery-saving auto-release." }
    ]
  }
];

// 12 Showcase Mobs for interactive preview
const PREVIEW_MOBS = [
  { id: "allay", name: "Allay", type: "Animated" },
  { id: "creeper", name: "Creeper", type: "Animated" },
  { id: "bee", name: "Bee", type: "Animated" },
  { id: "panda", name: "Panda", type: "Animated" },
  { id: "ghast", name: "Ghast", type: "Animated" },
  { id: "axolotl", name: "Axolotl", type: "Animated" },
  { id: "wolf", name: "Wolf", type: "Animated" },
  { id: "cat", name: "Black Cat", type: "Static" },
  { id: "fox", name: "Fox", type: "Static" },
  { id: "enderman", name: "Enderman", type: "Animated" },
  { id: "chicken", name: "Chicken", type: "Animated" },
  { id: "frog", name: "Frog", type: "Animated" }
];

document.addEventListener("DOMContentLoaded", () => {
  injectMetadata();
  initMobBrowser();
  initInstallStepSwitcher();
  initCopyChecksum();
  initDownloadToast();
  initScrollSpyAndProgress();
  initPlatformNotice();
  initChangelog();
  initVideoControls();
  initMobileNav();
});

// Inject metadata across all matching DOM elements
function injectMetadata() {
  document.querySelectorAll("[data-app]").forEach(el => {
    const prop = el.getAttribute("data-app");
    if (APP[prop]) {
      if (el.tagName === "A" && (prop === "apkUrl" || prop === "discordUrl" || prop === "githubUrl")) {
        el.href = APP[prop];
      } else {
        el.textContent = APP[prop];
      }
    }
  });
}

// Mob Browser Interactive Toggle
function initMobBrowser() {
  const grid = document.getElementById("mobPreviewGrid");
  const toggleButtons = document.querySelectorAll(".segment-btn");
  if (!grid) return;

  let currentMode = "anim"; // 'anim' | 'static'

  function renderGrid() {
    grid.innerHTML = "";
    PREVIEW_MOBS.forEach(mob => {
      const card = document.createElement("div");
      card.className = "mob-card block";
      
      const imgSrc = currentMode === "anim" 
        ? `assets/mobs/${mob.id}_anim.webp` 
        : `assets/mobs/${mob.id}_static.webp`;

      card.innerHTML = `
        <div class="mob-card-img-wrap slot">
          <img src="${imgSrc}" alt="${mob.name}" class="mob-card-img pixel-art" loading="lazy">
        </div>
        <span class="mob-card-name">${mob.name}</span>
        <span class="mob-card-chip">2x2 Widget</span>
      `;
      grid.appendChild(card);
    });

    // 13th Tile linking to app
    const moreCard = document.createElement("a");
    moreCard.href = "#install";
    moreCard.className = "mob-card mob-card-more block";
    moreCard.innerHTML = `
      <div style="display:flex; flex-direction:column; align-items:center; gap:8px;">
        <span style="font-size:24px;">+ 96</span>
        <span style="font-size:13px; font-weight:700;">More In App</span>
      </div>
    `;
    grid.appendChild(moreCard);
  }

  toggleButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      toggleButtons.forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      currentMode = btn.dataset.mode;
      renderGrid();
    });
  });

  renderGrid();
}

// Installation Step Visual Switcher
function initInstallStepSwitcher() {
  const stepCards = document.querySelectorAll(".step-card");
  const visualImg = document.getElementById("installVisualImg");
  const visualCaption = document.getElementById("installVisualCaption");
  if (!stepCards.length || !visualImg) return;

  const visuals = {
    1: { src: "assets/screens/step3_config.webp", caption: "Direct download without Play Store sign-in" },
    2: { src: "assets/screens/settings.webp", caption: "Standard Android setting for downloaded APKs" },
    3: { src: "assets/screens/carousel.webp", caption: "Fast package installation without background services" },
    4: { src: "assets/screens/step4_choose_mob.webp", caption: "Choose from 100+ animated & static mob variants" },
    5: { src: "assets/screens/step5_homescreen.webp", caption: "Place crisp pixel widgets right on your home screen" }
  };

  stepCards.forEach(card => {
    const stepNum = card.dataset.step;
    const activate = () => {
      stepCards.forEach(c => c.classList.remove("active"));
      card.classList.add("active");
      if (visuals[stepNum]) {
        visualImg.src = visuals[stepNum].src;
        if (visualCaption) visualCaption.textContent = visuals[stepNum].caption;
      }
    };

    card.addEventListener("click", activate);
    card.addEventListener("mouseenter", activate);
  });
}

// Copy SHA-256 Checksum
function initCopyChecksum() {
  const copyBtn = document.getElementById("copyChecksumBtn");
  const checksumText = document.getElementById("checksumValue");
  if (!copyBtn || !checksumText) return;

  copyBtn.addEventListener("click", async () => {
    try {
      await navigator.clipboard.writeText(APP.sha256);
      const originalHtml = copyBtn.innerHTML;
      copyBtn.innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
          <polyline points="20 6 9 17 4 12"></polyline>
        </svg>
      `;
      showToast("SHA-256 checksum copied to clipboard!");
      setTimeout(() => {
        copyBtn.innerHTML = originalHtml;
      }, 2000);
    } catch (err) {
      showToast("Copied hash to clipboard.");
    }
  });
}

// Download APK Toast Feedback
function initDownloadToast() {
  document.querySelectorAll(".download-trigger").forEach(btn => {
    btn.addEventListener("click", () => {
      showToast("Download started. Check your browser notifications for the APK.");
    });
  });
}

function showToast(message) {
  let toast = document.getElementById("appToast");
  if (!toast) {
    toast = document.createElement("div");
    toast.id = "appToast";
    toast.className = "toast";
    document.body.appendChild(toast);
  }
  toast.innerHTML = `
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
      <polyline points="7 10 12 15 17 10"></polyline>
      <line x1="12" y1="15" x2="12" y2="3"></line>
    </svg>
    <span>${message}</span>
  `;
  toast.classList.add("show");
  setTimeout(() => {
    toast.classList.remove("show");
  }, 3500);
}

// Scroll Progress & Nav Scroll-Spy
function initScrollSpyAndProgress() {
  const progressBar = document.getElementById("xpProgressBar");
  const navLinks = document.querySelectorAll(".nav-link[href^='#']");
  const sections = document.querySelectorAll("section[id]");

  window.addEventListener("scroll", () => {
    const scrollTop = window.scrollY;
    const docHeight = document.documentElement.scrollHeight - window.innerHeight;
    const progress = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0;
    if (progressBar) {
      progressBar.style.width = `${progress}%`;
    }

    // Scroll-Spy
    let currentId = "";
    sections.forEach(sec => {
      const top = sec.offsetTop - 120;
      if (scrollTop >= top) {
        currentId = sec.getAttribute("id");
      }
    });

    navLinks.forEach(link => {
      link.classList.remove("active");
      if (link.getAttribute("href") === `#${currentId}`) {
        link.classList.add("active");
      }
    });
  }, { passive: true });
}

// Platform detection for non-Android users
function initPlatformNotice() {
  const banner = document.getElementById("platformNotice");
  if (!banner) return;
  const ua = navigator.userAgent || "";
  const isAndroid = /android/i.test(ua);
  const isIOS = /iPad|iPhone|iPod/.test(ua);

  if (isIOS) {
    banner.style.display = "block";
    banner.innerHTML = "Note: Minecraft Mob Widgets is an Android APK app. iOS is not supported.";
  } else if (!isAndroid) {
    banner.style.display = "block";
    banner.innerHTML = "Opening this on a PC or Mac? Download the APK or visit on your Android phone to install.";
  }
}

// Changelog Renderer (used on index.html and changelog.html)
function initChangelog() {
  const latestList = document.getElementById("latestChangesList");
  if (latestList && CHANGELOG.length > 0) {
    const latest = CHANGELOG[0];
    latestList.innerHTML = latest.changes.slice(0, 4).map(c => `
      <li class="version-item">
        <span class="badge badge-${c.type}">${c.type}</span>
        <span>${c.text}</span>
      </li>
    `).join("");
  }

  const timelineContainer = document.getElementById("changelogTimeline");
  if (timelineContainer) {
    timelineContainer.innerHTML = CHANGELOG.map((rel, idx) => `
      <div class="timeline-node" id="v-${rel.version.replace(/\./g, '-')}">
        <div class="timeline-marker"></div>
        <div class="timeline-content block">
          <div class="timeline-header">
            <div>
              <span class="version-tag" style="font-size:28px;">v${rel.version}</span>
              <p style="margin-top:4px; font-weight:600; color:var(--ink);">${rel.title || ""}</p>
            </div>
            <span class="version-meta">${rel.date}</span>
          </div>
          <ul class="timeline-changes">
            ${rel.changes.map(ch => `
              <li>
                <span class="badge badge-${ch.type}">${ch.type}</span>
                <span>${ch.text}</span>
              </li>
            `).join("")}
          </ul>
          ${rel.downloadUrl ? `
            <div style="margin-top:8px;">
              <a href="${rel.downloadUrl}" class="btn btn-sm btn-secondary download-trigger">
                Download v${rel.version} APK
              </a>
            </div>
          ` : ""}
        </div>
      </div>
    `).join("");
  }
}

// Hero Video Play / Pause Accessibility Control
function initVideoControls() {
  const video = document.getElementById("heroVideo");
  const toggleBtn = document.getElementById("videoPlayToggle");
  if (!video || !toggleBtn) return;

  toggleBtn.addEventListener("click", () => {
    if (video.paused) {
      video.play();
      toggleBtn.innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <rect x="6" y="4" width="4" height="16"></rect>
          <rect x="14" y="4" width="4" height="16"></rect>
        </svg>
      `;
    } else {
      video.pause();
      toggleBtn.innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polygon points="5 3 19 12 5 21 5 3"></polygon>
        </svg>
      `;
    }
  });
}

// Mobile Hamburger Navigation
function initMobileNav() {
  const toggle = document.getElementById("mobileMenuBtn");
  const navList = document.getElementById("navLinks");
  if (!toggle || !navList) return;

  toggle.addEventListener("click", () => {
    navList.classList.toggle("mobile-open");
  });

  navList.querySelectorAll("a").forEach(a => {
    a.addEventListener("click", () => {
      navList.classList.remove("mobile-open");
    });
  });
}
