<script setup>

</script>

<template>
  <footer>
    <div class="container footer-content">
      <div class="footer-links">
        <a href="/about">회사 소개</a>
        <a href="/terms">이용약관</a>
        <a href="/privacy">개인정보처리방침</a>
        <a href="/contact">고객센터</a>
      </div>
      <div class="footer-copyright">&copy; 2023 Hot Item Collector. All rights reserved.</div>
    </div>
  </footer>
</template>

<style>
footer {
  margin-top: 60px;
  background-color: var(--footer-bg);
  padding: 30px 0;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  color: var(--text-color);
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: var(--main-color);
}

.footer-copyright {
  margin-top: 20px;
  text-align: center;
  width: 100%;
  font-size: 14px;
  color: #666;
}
</style>