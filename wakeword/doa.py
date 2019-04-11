# Add parent to path
import sys
import os
sys.path.insert(1, os.path.join(sys.path[0], '..'))

from wakeword.usb_4_mic_array.tuning import Tuning
import usb.core
import usb.util
import time
import threading


class DOA:
    """Gets approximate direction of arrival value from the microphone."""
    def __init__(self):
        """Finds microphone and initializes libraries."""
        self.usb_device = usb.core.find(idVendor=0x2886, idProduct=0x0018)

        if self.usb_device:
            self.mic_tuning = Tuning(self.usb_device)
        else:
            raise ("Unable to get ReSpeaker device.")

        self.sma_length = 6
        self.previous_values = []
        self.previous_sma = 0.0
        self.doa = 0.0
        self.thread = threading.Thread(target=self.run_doa)
        self.run = True
        self.thread.start()

    def run_doa(self):
        """Continuously checks and returns DOA value."""
        while self.run:
            raw_doa = self.mic_tuning.direction
            if raw_doa > 120:
                # Throw out outliers that would be behind Shimi
                continue
            else:
                if len(self.previous_values) < self.sma_length:
                    self.previous_values.append(raw_doa)
                    self.doa = sum(self.previous_values) / len(self.previous_values)
                    self.previous_sma = self.doa
                else:
                    last = self.previous_values.pop(0)
                    self.previous_values.append(raw_doa)
                    # From https://en.wikipedia.org/wiki/Moving_average
                    self.doa = self.previous_sma + (raw_doa / self.sma_length) - (last / self.sma_length)
                    self.previous_sma = self.doa
            time.sleep(0.1)

        usb.util.dispose_resources(self.usb_device)

    def stop(self):
        self.run = False
        self.thread.join()


if __name__ == "__main__":
    doa = DOA()

    while True:
        try:
            print(doa.doa)
            time.sleep(0.2)
        except KeyboardInterrupt:
            doa.stop()
            break