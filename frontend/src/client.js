const axios = require('axios');

const axiosInstance = axios.create({
  baseURL: 'http://13.125.60.206:8080',
});

module.exports = axiosInstance;