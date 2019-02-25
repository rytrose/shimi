import axios from 'axios'

export default () => {
  return axios.create({
    baseURL: 'https://shimi-webapp-server.serveo.net'
  })
}
