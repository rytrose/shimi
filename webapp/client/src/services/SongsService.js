import Api from '@/services/Api.js'

export default {
  fetchAllSongs (numResults = 100, offset = 0, query = {}) {
    return Api().get('songs', {
      params: {
        numResults,
        offset
      }
    })
  },
  fetchProcessedSongs (numResults = 100, offset = 0, query = {}) {
    return Api().get('processed', {
      params: {
        numResults,
        offset
      }
    })
  },
  fetchQueriedSongs (numResults = 100, offset = 0, query = {}) {
    return Api().get('queried', {
      params: {
        query,
        numResults,
        offset
      }
    })
  }
}
