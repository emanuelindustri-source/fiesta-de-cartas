(() => {
  "use strict";

  const EN = {
    skip: "Skip to content",
    navGames: "Games",
    navExperience: "Experience",
    navDownload: "Download",
    navPrivacy: "Privacy",
    heroEyebrow: "Made in Venezuela · Android",
    heroTitle: "The party plays at your table.",
    heroLead: "Five card and board games, a Spanish deck and every reason to celebrate. Play against bots or invite friends over WiFi — no internet, no accounts.",
    downloadApk: "Download APK",
    seeGames: "See the games",
    metaFree: "Free",
    metaAndroid: "Android 7+",
    metaLangs: "5 languages",
    metaNoAccount: "No accounts",
    statGames: "games",
    statLangs: "languages",
    statLevels: "levels",
    statAvatars: "avatars",
    statLan: "offline WiFi",
    gamesEyebrow: "Five ways to celebrate",
    gamesTitle: "Choose your table",
    gamesLead: "Every game has its own rhythm, rules and art. They share the same deck, the same party and the same promise: short matches that are easy to learn and hard to put down.",
    gameCaida: "The Venezuelan classic of calls and cunning. Call, capture and reach the target score before anyone else.",
    gameLudo: "Four tokens, a bouncing die and a race to the centre. Capture, advance and celebrate.",
    gameFiesta: "Match colours and numbers, use wilds and shout Fiesta! when you run out of cards.",
    gameKemix: "Memory and reflexes. Find the pairs of Venezuelan families before the bots do.",
    gameNervioso: "Discard, call a number and slap the pile first. The last to react takes the cards.",
    tagBotsWifi: "Bots + WiFi",
    tagBots: "Bots",
    moreSoon: "What comes next?",
    moreSoonText: "Tournaments, more avatars and a regulated online table. The roadmap is documented in the repository.",
    readRoadmap: "Read the roadmap",
    expEyebrow: "More than playing",
    expTitle: "It feels like a real table",
    expLead: "Deal card by card, swipe yours onto the table and feel the shake when someone calls. Avatars react, the table vibrates and every match has its own rhythm.",
    canto: "CALL!",
    featDeal: "Animated dealing",
    featDealText: "Cards leave the deck and fly to the table. You can throw yours too.",
    featShake: "The table shakes",
    featShakeText: "A call makes the table vibrate; a capture shakes it hard with sound.",
    featAvatars: "Expressive avatars",
    featAvatarsText: "Pick one of ten Venezuelan wildlife characters and watch them cheer or gasp.",
    featWifi: "WiFi without internet",
    featWifiText: "Create a room, share the QR and play with friends on the same network.",
    profileEyebrow: "Your space",
    profileTitle: "Level up, choose your avatar",
    profileLead: "Every match adds party points. Levels run from 1 to 100 and the curve is designed to last for months. Pick your character, customise your table and come back anytime: everything is saved on your phone.",
    chipLevels: "Levels 1–100",
    chipAvatars: "10 avatars",
    chipRules: "Configurable rules",
    chipSeasons: "Automatic seasons",
    downloadEyebrow: "Ready to install",
    downloadTitle: "Take the party in your pocket",
    downloadLead: "Download the signed APK, open it on Android and start playing. No account or connection is needed against bots.",
    allReleases: "All releases",
    installNote: "Android may ask for permission to install apps from this source. That is normal outside Google Play.",
    version: "Version",
    size: "Size",
    minAndroid: "Minimum Android",
    package: "Package",
    verifyNote: "Verify the hash before installing if you download from another mirror.",
    privacyEyebrow: "Transparency",
    privacyTitle: "Privacy without small print",
    privacyLead: "The app creates no accounts and uploads no matches. The camera only reads QR codes, local WiFi only carries the match and ads are served with consent.",
    privacyPolicy: "Privacy policy",
    privacyPolicyText: "What data the app touches, permissions and third parties.",
    terms: "Terms of use",
    termsText: "Licence, virtual chips and responsible use.",
    contact: "Contact",
    contactText: "Ideas, bugs or partnerships: write to us.",
    faqEyebrow: "Before you play",
    faqTitle: "Frequently asked questions",
    faq1q: "Is it free?",
    faq1a: "Yes. The app is free and includes ads. There are no mandatory purchases or subscriptions.",
    faq2q: "Do I need internet to play?",
    faq2a: "No. Bots work offline. Friend mode uses local WiFi: both phones must be on the same network or the host's hotspot.",
    faq3q: "Can I play for real money?",
    faq3a: "No. Chips are for practice and have no monetary value. Any real-money mode would require licensing, age verification and legal compliance; it is documented as future work, not active.",
    faq4q: "Which languages are included?",
    faq4a: "Spanish, English, Portuguese, Italian and Chinese. The app detects your phone language and you can change it in Settings.",
    faq5q: "Does it work on tablets or TV?",
    faq5a: "Yes. The interface adapts to phones, tablets, dual-screen devices and televisions; on TV you can navigate with the remote.",
    contactEyebrow: "Let's talk",
    contactTitle: "Got an idea for the party?",
    contactLead: "Write to us, tell us which game is missing or share the app with your table.",
    writeUs: "Write to the creator",
    footerMade: "Made with love for Venezuela and the world"
  };

  const html = document.documentElement;
  const nodes = [...document.querySelectorAll("[data-i18n]")];
  nodes.forEach((node) => { node.dataset.es = node.textContent; });

  const toggle = document.querySelector(".lang-toggle");
  const setLanguage = (lang) => {
    const english = lang === "en";
    nodes.forEach((node) => {
      const key = node.dataset.i18n;
      node.textContent = english ? (EN[key] || node.dataset.es) : node.dataset.es;
    });
    html.lang = english ? "en" : "es";
    if (toggle) {
      toggle.textContent = english ? "ES" : "EN";
      toggle.setAttribute("aria-label", english ? "Cambiar idioma a español" : "Switch language to English");
    }
    try { localStorage.setItem("fiesta-lang", english ? "en" : "es"); } catch (_) { /* private mode */ }
  };

  let stored = "es";
  try { stored = localStorage.getItem("fiesta-lang") || "es"; } catch (_) { /* ignore */ }
  if (stored === "en") setLanguage("en");
  if (toggle) toggle.addEventListener("click", () => setLanguage(html.lang === "en" ? "es" : "en"));

  const header = document.querySelector(".site-header");
  const onScroll = () => {
    if (header) header.classList.toggle("scrolled", window.scrollY > 12);
  };
  onScroll();
  window.addEventListener("scroll", onScroll, { passive: true });

  const nav = document.getElementById("site-nav");
  const navToggle = document.querySelector(".nav-toggle");
  if (nav && navToggle) {
    navToggle.addEventListener("click", () => {
      const open = nav.classList.toggle("open");
      navToggle.setAttribute("aria-expanded", String(open));
    });
    nav.querySelectorAll("a").forEach((link) => link.addEventListener("click", () => {
      nav.classList.remove("open");
      navToggle.setAttribute("aria-expanded", "false");
    }));
  }

  const year = document.getElementById("year");
  if (year) year.textContent = String(new Date().getFullYear());

  const revealItems = document.querySelectorAll(".reveal");
  if ("IntersectionObserver" in window) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("in");
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12, rootMargin: "0px 0px -40px 0px" });
    revealItems.forEach((item) => observer.observe(item));
  } else {
    revealItems.forEach((item) => item.classList.add("in"));
  }

  // Keep external links safe and don't leak the referrer to stores or ad networks.
  document.querySelectorAll('a[target="_blank"]').forEach((link) => {
    link.rel = "noopener noreferrer";
  });
})();
