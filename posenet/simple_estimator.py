from translation import format_posenet_shimi
import tensorflow as tf
from tensorflow import keras
import matplotlib.pyplot as plt
import numpy as np

train_input, train_labels = format_posenet_shimi(posenet_path="../data_collection/posenet_test",
                                      shimi_path="../data_collection/shimi_test")

test_input, test_labels = format_posenet_shimi(posenet_path="../data_collection/posenet_test",
                                                      shimi_path="../data_collection/shimi_test")

model = keras.Sequential()

model.add(keras.layers.Dense(15, activation="relu"))
model.add(keras.layers.Dense(10, activation="relu"))
model.add(keras.layers.Dense(5))
model.compile(optimizer=tf.train.AdamOptimizer(0.01),
              loss='mse',
              metrics=['mae']) 
model.fit(train_input, train_labels, epochs=150, batch_size=32)
loss, mae = model.evaluate(test_input, test_labels, batch_size=32)
predictions = model.predict(test_input, batch_size=32)
print(loss, mae)

a = plt.axes((0.0, 0.5, 1.0, 0.5))
b = plt.axes((0.0, 0.0, 1.0, 0.5))
t = np.linspace(0, test_labels.shape[0] * 0.01, test_labels.shape[0])

for i in range(5):
    a.plot(t, test_labels[:, i])
    b.plot(t, predictions[:, i])

a.legend([i + 1 for i in range(5)])
b.legend([i + 1 for i in range(5)])
plt.savefig('result.png')
