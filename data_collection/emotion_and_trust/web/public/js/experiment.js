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
    $('#waiting').show();
    oscClient.send("/get_trial", []);
}

let onQuadButton = (event) => {
    $('#chooseQuadrantContainer').hide();
    $('#waiting').show();
    let id = quadrantIDs.indexOf(event.target.id);
    oscClient.send("/trial_result", id);
    oscClient.send("/get_trial", []);
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
