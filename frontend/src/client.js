const axios = require('axios');

const axiosInstance = axios.create({
  baseURL: 'http://hotitemcollector.com:8080',
});

module.exports = axiosInstance;