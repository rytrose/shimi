from posenet.translation import format_posenet_shimi
import tensorflow as tf
from tensorflow import keras

train_input, train_labels = format_posenet_shimi(posenet_path="../data_collection/posenet_test",
                                      shimi_path="../data_collection/shimi_test")

test_input, test_labels = format_posenet_shimi(posenet_path="../data_collection/posenet_test",
                                                      shimi_path="../data_collection/shimi_test")


model = keras.Sequential()
model.add(keras.layers.Dense(5, activation="relu"))
model.compile(optimizer=tf.train.AdamOptimizer(0.01),
              loss='mse',       # mean squared error
              metrics=['mae'])  # mean absolute error
model.fit(train_input, train_labels, epochs=10, batch_size=32)
model.evaluate(test_input, test_labels, batch_size=32)
