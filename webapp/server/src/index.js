const Promise = require('bluebird')
const express = require('express')
const cors = require('cors')
const morgan = require('morgan')
const sqlite = require('sqlite')
const osc = require('osc')
const ws = require('ws')

const app = express()
app.use(morgan('combined'))
app.use(express.json())
app.use(cors())

let dbPath

if (process.arch === 'arm64') {
  dbPath = '/media/nvidia/disk2/shimi_library.db'
} else {
  dbPath = '/Volumes/Ryan_Drive/sqlite/shimi_library.db'
}

const dbPromise = sqlite.open(dbPath, {Promise})

/*
 * Set up OSC for communication with Python
 */
// Establishes a UDP address/port to receive from (local) and an address/port to send to (remote)
let udp = new osc.UDPPort({
  localAddress: '127.0.0.1',
  localPort: 6100,
  remoteAddress: '127.0.0.1',
  remotePort: 6101
})

// This function is called when the UDP port is opened and ready to use
udp.on('ready', () => {
  console.log('OSC Listening on port ' + udp.options.localPort + ', sending to port ' + udp.options.remotePort)
})

// This function is called when the UDP port gets a message
udp.on('message', function (message) {
  console.log(message.address, message.args)
})

// Open the UDP port
udp.open()

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
    let sqlString = 'SELECT * FROM songs WHERE ('
    let sqlParams = []

    if (query.title) {
      sqlString += ' title LIKE ? OR '
      sqlParams.push('%' + query.query + '%')
    }
    if (query.artist_name) {
      sqlString += ' artist_name LIKE ? OR '
      sqlParams.push('%' + query.query + '%')
    }
    if (query.release) {
      sqlString += ' release LIKE ? OR '
      sqlParams.push('%' + query.query + '%')
    }

    sqlString = sqlString.substring(0, sqlString.length - 3)
    sqlString += ') '

    if (query.processed === true) {
      sqlString += ' AND processed=1'
    }

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

app.get('/isProcessed', async (req, res, next) => {
  try {
    let msdId = req.query.msdId
    const db = await dbPromise

    const val = await db.get('SELECT processed FROM songs WHERE msd_id = ?', msdId)

    res.send(
      val
    )
  } catch (err) {
    next(err)
  }
})

app.post('/processed', async (req, res, next) => {
  try {
    let msdId = req.body.msdId
    const db = await dbPromise

    await db.run('UPDATE songs SET processed = 1 WHERE msd_id = ?', msdId)

    res.send(
      'ok'
    )
  } catch (err) {
    next(err)
  }
})

app.post('/sing', (req, res, next) => {
  let msdId = req.body.msdId
  let extractionType = req.body.extractionType

  let msg = {
    address: '/sing',
    args: [{
      type: 's',
      value: msdId
    },
    {
      type: 's',
      value: extractionType
    }]
  }

  udp.send(msg)

  res.send(
      'ok'
  )
})

app.post('/process', (req, res, next) => {
  let msdId = req.body.msdId

  let msg = {
    address: '/process',
    args: [{
      type: 's',
      value: msdId
    }]
  }

  udp.send(msg)

  res.send(
    'ok'
  )
})

const port = process.env.PORT || 8081
console.log('Listening on :' + port)
app.listen(port)
