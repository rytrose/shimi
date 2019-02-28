<template>
  <v-ons-page>
    <v-ons-toolbar>
      <div class="center">Shimi Library</div>
    </v-ons-toolbar>

    <v-ons-tabbar id="infinite-scroll-tabbar" position="auto"
                  @prechange="clear"
                  @postchange="changeTab">
      <template slot="pages">

        <!--Load all the songs-->
        <v-ons-page :infinite-scroll="loadMore">
          <v-ons-list>
            <v-ons-list-item>
              <div class="center">
                <v-ons-input placeholder="Query" float v-model="queryString">
                </v-ons-input>
              </div>
            </v-ons-list-item>
            <v-ons-list-item v-for="(queryParam, $index) in queryParams" :key="queryParam" tappable>
              <label class="left">
                <v-ons-checkbox
                  :input-id="'checkbox-' + $index"
                  :value="queryParam"
                  v-model="checkedQueryParams"
                >
                </v-ons-checkbox>
              </label>
              <label class="center" :for="'checkbox-' + $index">
                Search {{ queryParam }}
              </label>
            </v-ons-list-item>
          </v-ons-list>

          <v-ons-list>
            <v-ons-list-header>
              <v-ons-row>
                <v-ons-col width="40%"><h3>Title</h3></v-ons-col>
                <v-ons-col width="30%"><h3>Artist</h3></v-ons-col>
                <v-ons-col width="20%"><h3>Release</h3></v-ons-col>
                <v-ons-col width="10%"><h3>Processed?</h3></v-ons-col>
              </v-ons-row>
            </v-ons-list-header>

            <v-ons-list-item v-for="song in songs" :key="song.msd_id" @click="songClicked(song)" tappable>
              <v-ons-row>
                <v-ons-col width="40%">{{ song.title }}</v-ons-col>
                <v-ons-col width="30%">{{ song.artist_name }}</v-ons-col>
                <v-ons-col width="20%">{{ song.release }}</v-ons-col>
                <v-ons-col width="10%" class="center">{{ song.processed == 1 ? 'Yes' : 'No' }}</v-ons-col>
              </v-ons-row>
            </v-ons-list-item>
          </v-ons-list>

          <div class="after-list" v-show="loading">
            <v-ons-icon icon="fa-spinner" size="26px" spin></v-ons-icon>
          </div>

          <!--<v-ons-action-sheet-->
          <!--:visible.sync="songActionVisible"-->
          <!--cancelable-->
          <!--title="Song Action"-->
          <!--&gt;-->
          <!--<v-ons-action-sheet-button icon="md-square-o">Process</v-ons-action-sheet-button>-->
          <!--<v-ons-action-sheet-button icon="md-square-o">Sing</v-ons-action-sheet-button>-->
          <!--</v-ons-action-sheet>-->

        </v-ons-page>

        <!--Load processed songs-->
        <v-ons-page :infinite-scroll="loadMore">
          <v-ons-list>
            <v-ons-list-header>
              <v-ons-row>
                <v-ons-col width="40%"><h3>Title</h3></v-ons-col>
                <v-ons-col width="30%"><h3>Artist</h3></v-ons-col>
                <v-ons-col width="20%"><h3>Release</h3></v-ons-col>
                <v-ons-col width="10%"><h3>Processed?</h3></v-ons-col>
              </v-ons-row>
            </v-ons-list-header>

            <v-ons-list-item v-for="song in songs" :key="song.msd_id" @click="songClicked(song)" tappable>
              <v-ons-row>
                <v-ons-col width="40%">{{ song.title }}</v-ons-col>
                <v-ons-col width="30%">{{ song.artist_name }}</v-ons-col>
                <v-ons-col width="20%">{{ song.release }}</v-ons-col>
                <v-ons-col width="10%" class="center">{{ song.processed == 1 ? 'Yes' : 'No' }}</v-ons-col>
              </v-ons-row>
            </v-ons-list-item>
          </v-ons-list>

          <div class="after-list" v-show="loading">
            <v-ons-icon icon="fa-spinner" size="26px" spin></v-ons-icon>
          </div>

        </v-ons-page>

      </template>

      <v-ons-tab label="All Songs"></v-ons-tab>
      <v-ons-tab label="Processed Songs"></v-ons-tab>
    </v-ons-tabbar>

    <v-ons-action-sheet
      :visible.sync="songActionVisible"
      cancelable
    >
      <v-ons-action-sheet-button @click="processOrSing()">{{ isProcessed ? "Sing" : "Process" }}</v-ons-action-sheet-button>
      <v-ons-action-sheet-button modifier="destructive" @click="songActionVisible = false">Cancel
      </v-ons-action-sheet-button>
    </v-ons-action-sheet>

  </v-ons-page>
</template>

<script>
import SongsService from '@/services/SongsService'
import _ from 'lodash'

export default {
  name: 'ShimiLibrary',
  data () {
    return {
      songs: [],
      resultsLoaded: 0,
      fetchFn: SongsService.fetchAllSongs,
      loading: false,
      queryString: '',
      query: {},
      queryParams: ['Title', 'Artist', 'Release'],
      checkedQueryParams: ['Title'],
      songActionVisible: false,
      isProcessed: false,
      lastClickedID: ''
    }
  },
  created () {
    this.debouncedQuery = _.debounce(this.newQuery, 500)
  },
  mounted () {
    this.load()
  },
  methods: {
    async getAllSongs () {
      const response = await SongsService.fetchAllSongs()
      let numResults = response.data.length
      this.songs = this.songs.concat(response.data)
      this.resultsLoaded += numResults
    },
    async loadMore (done) {
      this.load()
      done()
    },
    async load () {
      this.loading = true
      const response = await this.fetchFn(undefined, this.resultsLoaded, this.query)
      let numResults = response.data.length
      this.songs = this.songs.concat(response.data)
      this.resultsLoaded += numResults
      this.loading = false
    },
    async songClicked (song) {
      this.isProcessed = false
      const response = await SongsService.isProcessed(song.msd_id)
      this.isProcessed = response.data.processed === 1
      this.songActionVisible = true
      this.lastClickedID = song.msd_id
    },
    clear () {
      this.songs = []
      this.resultsLoaded = 0
      this.query = {}
    },
    async changeTab (e) {
      let tabIndex = e.index
      switch (tabIndex) {
        case 1:
          this.fetchFn = SongsService.fetchProcessedSongs
          break
        default:
          this.fetchFn = SongsService.fetchAllSongs
      }
      await this.load()
    },
    newQuery () {
      if (this.query !== '' && this.checkedQueryParams.length > 0) {
        this.clear()

        let title = false
        let artistName = false
        let release = false

        for (let param of this.checkedQueryParams) {
          switch (param) {
            case 'Title':
              title = true
              break
            case 'Artist':
              artistName = true
              break
            case 'Release':
              release = true
              break
          }
        }

        this.query = {
          query: this.queryString,
          title: title,
          artist_name: artistName,
          release: release
        }
        this.fetchFn = SongsService.fetchQueriedSongs
        this.load()
      }
    },
    processOrSing () {
      if (this.isProcessed) {
        // Sing
        SongsService.sing(this.lastClickedID, 'cnn')
      } else {
        // Process
        SongsService.process(this.lastClickedID)
      }
      this.songActionVisible = false
    }
  },
  watch: {
    queryString: function (newQuery, oldQuery) {
      if (this.queryString === '' && this.fetchFn !== SongsService.fetchAllSongs) {
        this.clear()
        this.fetchFn = SongsService.fetchAllSongs
        this.load()
      } else {
        this.debouncedQuery()
      }
    }
  }
}
</script>

<style scoped>
  ons-input {
    width: 100%;
  }

  .center {
    text-align: center;
  }

  .after-list {
    margin: 20px;
    text-align: center;
  }
</style>
