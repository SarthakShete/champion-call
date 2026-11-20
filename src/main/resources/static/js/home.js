/* ---- Scroll reveal ---- */
const observer = new IntersectionObserver(
  (entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) entry.target.classList.add("show");
    });
  },
  { threshold: 0.1 }
);
document.querySelectorAll(".hidden").forEach((el) => observer.observe(el));

/* ---- Canvas particle background ---- */
const canvas = document.getElementById("particles");
const ctx = canvas.getContext("2d");
let W,
  H,
  particles = [];

function resize() {
  W = canvas.width = canvas.offsetWidth;
  H = canvas.height = canvas.offsetHeight;
}
resize();
window.addEventListener("resize", resize);

function Particle() {
  this.x = Math.random() * W;
  this.y = Math.random() * H;
  this.r = Math.random() * 2 + 0.5;
  this.dx = (Math.random() - 0.5) * 0.4;
  this.dy = (Math.random() - 0.5) * 0.4;
  this.alpha = Math.random() * 0.5 + 0.1;
}

for (let i = 0; i < 120; i++) particles.push(new Particle());

function draw() {
  ctx.clearRect(0, 0, W, H);
  particles.forEach((p) => {
    ctx.beginPath();
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
    ctx.fillStyle = `rgba(255,145,77,${p.alpha})`;
    ctx.fill();
    p.x += p.dx;
    p.y += p.dy;
    if (p.x < 0 || p.x > W) p.dx *= -1;
    if (p.y < 0 || p.y > H) p.dy *= -1;
  });

  /* Connect nearby particles with lines */
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dist = Math.hypot(
        particles[i].x - particles[j].x,
        particles[i].y - particles[j].y
      );
      if (dist < 120) {
        ctx.beginPath();
        ctx.moveTo(particles[i].x, particles[i].y);
        ctx.lineTo(particles[j].x, particles[j].y);
        ctx.strokeStyle = `rgba(255,145,77,${0.08 * (1 - dist / 120)})`;
        ctx.lineWidth = 0.6;
        ctx.stroke();
      }
    }
  }
  requestAnimationFrame(draw);
}
draw();

function setRole(role) {
  console.log("The role is : ", role);

  document.cookie = "Role=" + role + "; path=/";

  window.location.href = "/login";
}
