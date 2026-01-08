// Modern Theme JavaScript

// DOM Elements
const mobileMenuBtn = document.getElementById('mobileMenuBtn');
const mobileNav = document.getElementById('mobileNav');

// Mobile Menu Toggle
if (mobileMenuBtn && mobileNav) {
    mobileMenuBtn.addEventListener('click', () => {
        mobileNav.classList.toggle('mobile-nav-open');
        const icon = mobileMenuBtn.querySelector('i');
        if (mobileNav.classList.contains('mobile-nav-open')) {
            icon.classList.remove('fa-bars');
            icon.classList.add('fa-times');
        } else {
            icon.classList.remove('fa-times');
            icon.classList.add('fa-bars');
        }
    });
}

// Close mobile menu when clicking on a link
const mobileNavLinks = document.querySelectorAll('.mobile-nav .nav-link');
mobileNavLinks.forEach(link => {
    link.addEventListener('click', () => {
        mobileNav.classList.remove('mobile-nav-open');
        const icon = mobileMenuBtn.querySelector('i');
        icon.classList.remove('fa-times');
        icon.classList.add('fa-bars');
    });
});

// Fix TOC Structure Function
function fixTOCStructure() {
    const tocElements = document.querySelectorAll('.toc');
    
    tocElements.forEach(toc => {
        const olElements = toc.querySelectorAll('ol');
        
        olElements.forEach(ol => {
            const parent = ol.parentNode;
            if (parent && parent.tagName === 'OL') {
                const previousLi = ol.previousElementSibling;
                if (previousLi && previousLi.tagName === 'LI') {
                    previousLi.appendChild(ol);
                }
            }
        });
    });
}

// Smooth Scrolling for Anchor Links with Number ID Support
function initSmoothScrolling() {
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    
    anchorLinks.forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;
            
            const id = targetId.substring(1);
            const targetElement = document.getElementById(id);
            
            if (targetElement) {
                window.scrollTo({
                    top: targetElement.offsetTop - 80, // Account for header height
                    behavior: 'smooth'
                });
            }
        });
    });
}

// Back to Top Button
const backToTopBtn = document.createElement('button');
backToTopBtn.innerHTML = '<i class="fas fa-arrow-up"></i>';
backToTopBtn.className = 'back-to-top';
backToTopBtn.style.cssText = `
    position: fixed;
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
    border: none;
    border-radius: 50%;
    background-color: var(--primary-color);
    color: white;
    font-size: 20px;
    cursor: pointer;
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
    z-index: 999;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
`;

document.body.appendChild(backToTopBtn);

window.addEventListener('scroll', () => {
    if (window.pageYOffset > 300) {
        backToTopBtn.style.opacity = '1';
        backToTopBtn.style.visibility = 'visible';
    } else {
        backToTopBtn.style.opacity = '0';
        backToTopBtn.style.visibility = 'hidden';
    }
});

backToTopBtn.addEventListener('click', () => {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
});

// Theme Toggle (Light/Dark Mode)
const themeToggleBtn = document.createElement('button');
themeToggleBtn.innerHTML = '<i class="fas fa-moon"></i>';
themeToggleBtn.className = 'theme-toggle';
themeToggleBtn.style.cssText = `
    position: fixed;
    top: 100px;
    right: 20px;
    width: 50px;
    height: 50px;
    border: none;
    border-radius: 50%;
    background-color: var(--bg-secondary);
    color: var(--text-primary);
    font-size: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    z-index: 999;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
`;

document.body.appendChild(themeToggleBtn);

// Check for saved theme preference
const savedTheme = localStorage.getItem('theme');
if (savedTheme) {
    document.documentElement.setAttribute('data-theme', savedTheme);
    if (savedTheme === 'dark') {
        themeToggleBtn.innerHTML = '<i class="fas fa-sun"></i>';
    }
}

// Toggle theme function
function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    let newTheme;
    
    if (currentTheme === 'dark') {
        newTheme = 'light';
        themeToggleBtn.innerHTML = '<i class="fas fa-moon"></i>';
    } else {
        newTheme = 'dark';
        themeToggleBtn.innerHTML = '<i class="fas fa-sun"></i>';
    }
    
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    
    // Update CSS variables for dark theme
    updateThemeVariables(newTheme);
}

// Update CSS variables based on theme
function updateThemeVariables(theme) {
    const root = document.documentElement;
    
    if (theme === 'dark') {
        root.style.setProperty('--bg-primary', '#111827');
        root.style.setProperty('--bg-secondary', '#1f2937');
        root.style.setProperty('--bg-tertiary', '#374151');
        root.style.setProperty('--text-primary', '#f9fafb');
        root.style.setProperty('--text-secondary', '#d1d5db');
        root.style.setProperty('--border-color', '#374151');
    } else {
        // Reset to default light theme
        root.style.setProperty('--bg-primary', '#ffffff');
        root.style.setProperty('--bg-secondary', '#f9fafb');
        root.style.setProperty('--bg-tertiary', '#f3f4f6');
        root.style.setProperty('--text-primary', '#1f2937');
        root.style.setProperty('--text-secondary', '#6b7280');
        root.style.setProperty('--border-color', '#e5e7eb');
    }
}

themeToggleBtn.addEventListener('click', toggleTheme);

// Add hover effects to cards
const cards = document.querySelectorAll('.post-preview, .widget');
cards.forEach(card => {
    card.addEventListener('mouseenter', () => {
        card.style.transform = 'translateY(-2px)';
    });
    
    card.addEventListener('mouseleave', () => {
        card.style.transform = 'translateY(0)';
    });
});

// Animate elements on scroll
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe all animated elements
const animatedElements = document.querySelectorAll('.post, .post-preview, .page, .widget');
animatedElements.forEach(el => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(20px)';
    el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
    observer.observe(el);
});

// Highlight current active link based on scroll position
function highlightActiveLink() {
    const sections = document.querySelectorAll('section[id]');
    const scrollPosition = window.pageYOffset + 100;
    
    sections.forEach(section => {
        const sectionTop = section.offsetTop;
        const sectionHeight = section.offsetHeight;
        const sectionId = section.getAttribute('id');
        const correspondingLink = document.querySelector(`a[href="#${sectionId}"]`);
        
        if (correspondingLink) {
            if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
                correspondingLink.classList.add('active');
            } else {
                correspondingLink.classList.remove('active');
            }
        }
    });
}

window.addEventListener('scroll', highlightActiveLink);

// Initialize
window.addEventListener('DOMContentLoaded', () => {
    // Set initial theme variables
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
    updateThemeVariables(currentTheme);
    
    // Fix TOC structure
    fixTOCStructure();
    
    // Initialize smooth scrolling
    initSmoothScrolling();
    
    // Highlight active link on page load
    highlightActiveLink();
});