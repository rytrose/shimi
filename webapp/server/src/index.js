const Promise = require('bluebird')
const express = require('express')
const cors = require('cors')
const morgan = require('morgan')
const sqlite = require('sqlite')

const app = express()
app.use(morgan('combined'))
app.use(express.json())
app.use(cors())

const dbPath = '/Volumes/Ryan_Drive/sqlite/shimi_library.db'
const dbPromise = sqlite.open(dbPath, {Promise})

app.get('/songs', async (req, res, next) => {
  try {
    const db = await dbPromise
    const songs = await Promise.all(
      db.all('SELECT * FROM songs ORDER BY title ASC LIMIT ? OFFSET ? ', req.query.numResults, req.query.offset)
    )
    res.send(
      songs
    )
  } catch (err) {
    next(err)
  }
})

app.get('/processed', async (req, res, next) => {
  try {
    const db = await dbPromise
    const songs = await Promise.all(
      db.all('SELECT * FROM songs WHERE processed=1 ORDER BY title ASC LIMIT ? OFFSET ? ', req.query.numResults, req.query.offset)
    )
    res.send(
      songs
    )
  } catch (err) {
    next(err)
  }
})

app.get('/queried', async (req, res, next) => {
  try {
    let query = JSON.parse(req.query.query)
    const db = await dbPromise
    let sqlString = 'SELECT * FROM songs WHERE'
    let sqlParams = []

    if (query.title) {
      sqlString += ' title LIKE ?, '
      sqlParams.push('%' + query.query + '%')
    }
    if (query.artist_name) {
      sqlString += ' artist_name LIKE ?, '
      sqlParams.push('%' + query.query + '%')
    }
    if (query.release) {
      sqlString += ' release LIKE ?, '
      sqlParams.push('%' + query.query + '%')
    }

    sqlString = sqlString.substring(0, sqlString.length - 2)

    sqlString += ' ORDER BY title ASC LIMIT ? OFFSET ?'
    sqlParams.push(req.query.numResults)
    sqlParams.push(req.query.offset)

    const songs = await Promise.all(
      db.all(sqlString, sqlParams)
    )

    res.send(
      songs
    )
  } catch (err) {
    next(err)
  }
})

const port = process.env.PORT || 8081
console.log('Listening on :' + port)
app.listen(port)
