document.addEventListener("DOMContentLoaded", function () {
  const navbar = document.getElementById("navbar");
  const btn = document.querySelector(".profile-btn");
  const dropdown = document.getElementById("profileDropdown");

  // Darken navbar on scroll
  if (navbar) {
    const onScroll = () =>
      navbar.classList.toggle("scrolled", window.scrollY > 20);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
  }

  if (!btn || !dropdown) return;

  const close = () => {
    dropdown.classList.remove("open");
    btn.setAttribute("aria-expanded", "false");
  };

  btn.addEventListener("click", function (e) {
    e.stopPropagation();
    const open = dropdown.classList.toggle("open");
    btn.setAttribute("aria-expanded", open);
  });
  document.addEventListener("click", close);
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") close();
  });
});
