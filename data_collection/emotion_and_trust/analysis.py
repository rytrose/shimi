import pickle
import glob
import pandas as pd
from sklearn.metrics import confusion_matrix, classification_report
from scipy import stats
import statsmodels.stats.api as sms
import matplotlib.pyplot as plt
import itertools
import numpy as np


def plot_confusion_matrix(cm,
                          target_names,
                          filename="cm_out.png",
                          title='Confusion matrix',
                          cmap=None,
                          normalize=True):
    """
    given a sklearn confusion matrix (cm), make a nice plot

    Arguments
    ---------
    cm:           confusion matrix from sklearn.metrics.confusion_matrix

    target_names: given classification classes such as [0, 1, 2]
                  the class names, for example: ['high', 'medium', 'low']

    title:        the text to display at the top of the matrix

    cmap:         the gradient of the values displayed from matplotlib.pyplot.cm
                  see http://matplotlib.org/examples/color/colormaps_reference.html
                  plt.get_cmap('jet') or plt.cm.Blues

    normalize:    If False, plot the raw numbers
                  If True, plot the proportions

    Usage
    -----
    plot_confusion_matrix(cm           = cm,                  # confusion matrix created by
                                                              # sklearn.metrics.confusion_matrix
                          normalize    = True,                # show proportions
                          target_names = y_labels_vals,       # list of names of the classes
                          title        = best_estimator_name) # title of graph

    Citiation
    ---------
    http://scikit-learn.org/stable/auto_examples/model_selection/plot_confusion_matrix.html

    """

    print("computing accuracy")
    accuracy = np.trace(cm) / float(np.sum(cm))
    misclass = 1 - accuracy

    print("getting cmap")
    if cmap is None:
        cmap = plt.get_cmap('Blues')

    print("making fig")
    plt.figure(figsize=(8, 6))
    plt.imshow(cm, interpolation='nearest', cmap=cmap)
    plt.title(title)
    plt.colorbar()

    print("made fig")

    if target_names is not None:
        tick_marks = np.arange(len(target_names))
        plt.xticks(tick_marks, target_names, rotation=45)
        plt.yticks(tick_marks, target_names)

    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]

    print("Iterating...")
    thresh = cm.max() / 1.5 if normalize else cm.max() / 2
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        if normalize:
            plt.text(j, i, "{:0.4f}".format(cm[i, j]),
                     horizontalalignment="center",
                     color="white" if cm[i, j] > thresh else "black")
        else:
            plt.text(j, i, "{:,}".format(cm[i, j]),
                     horizontalalignment="center",
                     color="white" if cm[i, j] > thresh else "black")

    plt.tight_layout()
    plt.ylabel('True label')
    plt.xlabel('Predicted label\naccuracy={:0.4f}; misclass={:0.4f}'.format(accuracy, misclass))
    print("Saving...")
    plt.savefig(filename)


def make_df():
    files = glob.glob("results/*.p")
    timestamp = []
    group = []
    trial_type = []
    with_audio = []
    ground_truth = []
    reported = []

    for f in files:
        f_obj = pickle.load(open(f, 'rb'))
        f_timestamp = f.split('/')[-1].split('.')[0]
        f_group = f_obj['group']

        for r in f_obj['results']:
            timestamp.append(f_timestamp)
            group.append(f_group)
            trial_type.append(r['trial_type'])
            with_audio.append(r['with_audio'])
            ground_truth.append(r['ground_truth'])
            reported.append(r['reported'])

    return pd.DataFrame.from_dict({
        'timestamp': timestamp,
        'group': group,
        'trial_type': trial_type,
        'with_audio': with_audio,
        'ground_truth': ground_truth,
        'reported': reported
    })


def analyze(data):
    shimi_spoken_grouped = data.groupby("group")
    overall_shimi_cm = None
    overall_shimi_report = None
    audio_only_shimi_cm = None
    audio_only_shimi_report = None
    random_audio_shimi_cm = None
    random_audio_shimi_report = None
    random_silent_shimi_cm = None
    random_silent_shimi_report = None
    linked_audio_shimi_cm = None
    linked_audio_shimi_report = None
    linked_silent_shimi_cm = None
    linked_silent_shimi_report = None

    overall_spoken_confusion_matrix = None
    overall_spoken_report = None
    audio_only_spoken_cm = None
    audio_only_spoken_report = None
    random_audio_spoken_cm = None
    random_audio_spoken_report = None
    random_silent_spoken_cm = None
    random_silent_spoken_report = None
    linked_audio_spoken_cm = None
    linked_audio_spoken_report = None
    linked_silent_spoken_cm = None
    linked_silent_spoken_report = None

    for key, item in shimi_spoken_grouped:
        truth = item['ground_truth'].tolist()
        reported = item['reported'].tolist()
        if key == 'shimivoice':
            overall_shimi_cm = confusion_matrix(truth, reported)
            overall_shimi_report = classification_report(truth, reported)
        else:
            overall_spoken_confusion_matrix = confusion_matrix(truth, reported)
            overall_spoken_report = classification_report(truth, reported)

        trial_type_grouped = item.groupby("trial_type")

        for trial_key, trial_item in trial_type_grouped:
            trial_truth = trial_item['ground_truth'].tolist()
            trial_reported = trial_item['reported'].tolist()
            if key == 'shimivoice':
                if trial_key == 'audioonly':
                    audio_only_shimi_cm = confusion_matrix(trial_truth, trial_reported,
                                                           labels=["angry", "calm", "happy", "sad"])
                    audio_only_shimi_report = classification_report(trial_truth, trial_reported)
                elif trial_key == 'randomgesture':
                    audio_grouped = trial_item.groupby("with_audio")

                    for audio_key, audio_item in audio_grouped:
                        audio_truth = audio_item['ground_truth'].tolist()
                        audio_reported = audio_item['reported'].tolist()
                        if audio_key:
                            random_audio_shimi_cm = confusion_matrix(audio_truth, audio_reported,
                                                                     labels=["angry", "calm", "happy", "sad"])
                            random_audio_shimi_report = classification_report(audio_truth, audio_reported)
                        else:
                            random_silent_shimi_cm = confusion_matrix(audio_truth, audio_reported,
                                                                      labels=["angry", "calm", "happy", "sad"])
                            random_silent_shimi_report = classification_report(audio_truth, audio_reported)

                else:
                    audio_grouped = trial_item.groupby("with_audio")

                    for audio_key, audio_item in audio_grouped:
                        audio_truth = audio_item['ground_truth'].tolist()
                        audio_reported = audio_item['reported'].tolist()
                        if audio_key:
                            linked_audio_shimi_cm = confusion_matrix(audio_truth, audio_reported,
                                                                     labels=["angry", "calm", "happy", "sad"])
                            linked_audio_shimi_report = classification_report(audio_truth, audio_reported)
                        else:
                            linked_silent_shimi_cm = confusion_matrix(audio_truth, audio_reported,
                                                                      labels=["angry", "calm", "happy", "sad"])
                            linked_silent_shimi_report = classification_report(audio_truth, audio_reported)
            else:
                if trial_key == 'audioonly':
                    audio_only_spoken_cm = confusion_matrix(trial_truth, trial_reported)
                    audio_only_spoken_report = classification_report(trial_truth, trial_reported)
                elif trial_key == 'randomgesture':
                    audio_grouped = trial_item.groupby("with_audio")

                    for audio_key, audio_item in audio_grouped:
                        audio_truth = audio_item['ground_truth'].tolist()
                        audio_reported = audio_item['reported'].tolist()
                        if audio_key:
                            random_audio_spoken_cm = confusion_matrix(audio_truth, audio_reported,
                                                                      labels=["angry", "calm", "happy", "sad"])
                            random_audio_spoken_report = classification_report(audio_truth, audio_reported)
                        else:
                            random_silent_spoken_cm = confusion_matrix(audio_truth, audio_reported,
                                                                       labels=["angry", "calm", "happy", "sad"])
                            random_silent_spoken_report = classification_report(audio_truth, audio_reported)

                else:
                    audio_grouped = trial_item.groupby("with_audio")

                    for audio_key, audio_item in audio_grouped:
                        audio_truth = audio_item['ground_truth'].tolist()
                        audio_reported = audio_item['reported'].tolist()
                        if audio_key:
                            linked_audio_spoken_cm = confusion_matrix(audio_truth, audio_reported,
                                                                      labels=["angry", "calm", "happy", "sad"])
                            linked_audio_spoken_report = classification_report(audio_truth, audio_reported)
                        else:
                            linked_silent_spoken_cm = confusion_matrix(audio_truth, audio_reported,
                                                                       labels=["angry", "calm", "happy", "sad"])
                            linked_silent_spoken_report = classification_report(audio_truth, audio_reported)

    print("Shimi voice overall:")
    print(overall_shimi_cm)
    # plot_confusion_matrix(overall_shimi_cm, title='Shimi Voice Overall',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='shimi_overall.png')
    print(overall_shimi_report)

    print("--")

    print("Shimi audio only:")
    print(audio_only_shimi_cm)
    # plot_confusion_matrix(audio_only_shimi_cm, title='Shimi Audio Only',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='shimi_audio_only.png')
    print(audio_only_shimi_report)

    print("--")

    print("Shimi random gesture with audio:")
    print(random_audio_shimi_cm)
    # plot_confusion_matrix(random_audio_shimi_cm, title='Shimi Random Gesture With Audio',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='shimi_random_audio.png')
    print(random_audio_shimi_report)

    print("--")

    print("Shimi random gesture no audio:")
    print(random_silent_shimi_cm)
    # plot_confusion_matrix(random_silent_shimi_cm, title='Shimi Random Gesture No Audio',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='shimi_random_no_audio.png')
    print(random_silent_shimi_report)

    print("--")

    print("Shimi linked gesture with audio:")
    print(linked_audio_shimi_cm)
    # plot_confusion_matrix(linked_audio_shimi_cm, title='Shimi Generated Gesture With Audio',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='shimi_generated_audio.png')
    print(linked_audio_shimi_report)

    print("--")

    print("Shimi linked gesture no audio:")
    print(linked_silent_shimi_cm)
    # plot_confusion_matrix(linked_silent_shimi_cm, title='Shimi Generated Gesture No Audio',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='shimi_generated_no_audio.png')
    print(linked_silent_shimi_report)

    print("----------------------------------")

    print("Spoken voice overall:")
    print(overall_spoken_confusion_matrix)
    # plot_confusion_matrix(overall_spoken_confusion_matrix, title='Spoken Voice Overall',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='spoken_overall.png')
    print(overall_spoken_report)

    print("--")

    print("Spoken audio only:")
    print(audio_only_spoken_cm)
    # plot_confusion_matrix(audio_only_spoken_cm, title='Spoken Audio Only',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='spoken_audio_only.png')
    print(audio_only_spoken_report)

    print("--")

    print("Spoken random gesture with audio:")
    print(random_audio_spoken_cm)
    # plot_confusion_matrix(random_audio_spoken_cm, title='Spoken Random Gesture With Audio',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='spoken_random_audio.png')
    print(random_audio_spoken_report)

    print("--")

    print("Spoken random gesture no audio:")
    print(random_silent_spoken_cm)
    # plot_confusion_matrix(random_silent_spoken_cm, title='Spoken Random Gesture No Audio',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='spoken_random_no_audio.png')
    print(random_silent_spoken_report)

    print("--")

    print("Spoken linked gesture with audio:")
    print(linked_audio_spoken_cm)
    # plot_confusion_matrix(linked_audio_spoken_cm, title='Spoken Generated Gesture With Audio',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='spoken_generated_audio.png')
    print(linked_audio_spoken_report)

    print("--")

    print("Spoken linked gesture no audio:")
    print(linked_silent_spoken_cm)
    # plot_confusion_matrix(linked_silent_spoken_cm, title='Spoken Generated Gesture No Audio',
    #                       target_names=["angry", "calm", "happy", "sad"], normalize=False,
    #                       filename='spoken_generated_no_audio.png')
    print(linked_silent_spoken_report)


def ttests(data):
    shimi_trials = data.groupby("group").get_group('shimivoice').groupby('with_audio').get_group(True).groupby(
        'trial_type')

    random_trials = shimi_trials.get_group('randomgesture').groupby('timestamp')
    random_trials_acc = []

    for t, random_trial in random_trials:
        correct = random_trial[random_trial['ground_truth'] == random_trial['reported']].count()[0]
        total = random_trial.count()[0]
        acc = correct / total
        print("Random: (%s) %d / %d (%f)" % (t, correct, total, acc))
        if total == 8:
            random_trials_acc.append(acc)

    generated_trials = shimi_trials.get_group('linkedgesture').groupby('timestamp')
    generated_trials_acc = []

    for t, generated_trial in generated_trials:
        correct = generated_trial[generated_trial['ground_truth'] == generated_trial['reported']].count()[0]
        total = generated_trial.count()[0]
        acc = correct / total
        print("Generated: (%s) %d / %d (%f)" % (t, correct, total, acc))
        if(total == 8):
            generated_trials_acc.append(acc)

    print(len(random_trials_acc), len(generated_trials_acc))
    print(random_trials_acc, generated_trials_acc)
    print(stats.ttest_ind(random_trials_acc, generated_trials_acc))
    cm = sms.CompareMeans(sms.DescrStatsW(random_trials_acc), sms.DescrStatsW(generated_trials_acc))
    print(cm.summary())
    print(cm.tconfint_diff(usevar='pooled'))

if __name__ == '__main__':
    data = make_df()
    # analyze(data)
    ttests(data)
