import { createApp } from 'vue';
import App from '@/App';
import router from '@/router';

//use this configure for local testing
//axios.defaults.baseURL = 'http://localhost:8080';
//axios.defaults.baseURL = 'http://13.125.60.206:8080';

const app = createApp(App);
//app.config.globalProperties.axios = axios;
app.use(router);

app.mount('#app');
