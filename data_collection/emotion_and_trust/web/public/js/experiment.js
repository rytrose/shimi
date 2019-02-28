let oscClient = null;
let quadrantIDs = ['happy', 'calm', 'sad', 'angry'];

$(document).ready(() => {
    let oscClient = setupOSC();
    setupOnClicks();
});

let setupOSC = () => {
    oscClient = new OSCClient();
    oscClient.map("/trial_started", onTrialStarted);
    oscClient.map("/trial_ended", onTrialEnded);
    oscClient.map("/complete", onComplete);
}

let setupOnClicks = () => {
    $("#beginForm").on('submit', onBegin);
    $(".quadButton").on('click', onQuadButton);
}

let onBegin = (event) => {
    event.preventDefault();
    $('#beginContainer').hide();

    let timer = new Timer();
    $('#timerContainer').show();
    timer.start({countdown: true, startValues: {seconds: 5}});
    $('#timer').html(timer.getTimeValues().seconds.toString()).fadeIn();
    timer.addEventListener('secondsUpdated', function (e) {
            $('#timer').html(timer.getTimeValues().seconds.toString());
    });
    timer.addEventListener('targetAchieved', function (e) {
        $('#timerContainer').hide();
        $('#waiting').show();
        oscClient.send("/get_trial", []);
    });
}

let onQuadButton = (event) => {
    $('#chooseQuadrantContainer').hide();
    let id = quadrantIDs.indexOf(event.target.id);
    oscClient.send("/trial_result", id);

    let timer = new Timer();
    $('#timerContainer').show();
    timer.start({countdown: true, startValues: {seconds: 5}});
    $('#timer').html(timer.getTimeValues().seconds.toString()).fadeIn();
    timer.addEventListener('secondsUpdated', function (e) {
            $('#timer').html(timer.getTimeValues().seconds.toString());
    });
    timer.addEventListener('targetAchieved', function (e) {
        $('#timerContainer').hide();
        $('#waiting').show();
        oscClient.send("/get_trial", []);
    });
}

let onTrialStarted = (address, args) => {
    $('#waiting').hide();
    $('#trialStartedContainer').show();
}

let onTrialEnded = (address, args) => {
    $('#trialStartedContainer').hide();
    $('#chooseQuadrantContainer').show();
}

let onComplete = (address, args) => {
    $('#waiting').hide();
    $('#complete').show();
}
