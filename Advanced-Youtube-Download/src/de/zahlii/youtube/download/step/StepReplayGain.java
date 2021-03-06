package de.zahlii.youtube.download.step;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.zahlii.youtube.download.QueueEntry;
import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.cli.CLI;
import de.zahlii.youtube.download.cli.ProcessAdapter;

public class StepReplayGain extends Step {
	private class GainProcessAdapter extends ProcessAdapter {
		private double volumeGain = 0;

		public double getVolume() {
			return volumeGain;
		}

		@Override
		public void processLineOut(final String line) {
			if (line.contains("bytes analyzed")) {
				final String p = line.split("%")[0].trim();
				final int prog = Integer.parseInt(p);
				reportProgress(prog / 100.0);
			}
			if (line.contains("No changes to")) {
				volumeGain = 0;
			}
			if (line.contains("Applying mp3 gain change of")) {
				final int v = Integer.parseInt(line.split("of ")[1].split(" to")[0]);
				volumeGain = v;
			}
		}
	}

	private double volume;

	public StepReplayGain(final QueueEntry entry) {
		super(entry, new StepDescriptor("ReplayGain", "Normalizes the perceived audio volume."));
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doStep() {

		final ProcessBuilder n = new ProcessBuilder();

		final File f = entry.getFinalMP3File();
		if (f == null) {
			nextStep();
			return;
		}

		if (entry.isFLAC()) {
			final List<String> meta = new ArrayList<String>();
			meta.add(ConfigManager.METAFLAC.getAbsolutePath());
			meta.add("--add-replay-gain");
			meta.add(f.getAbsolutePath());
			n.command(meta);

			final CLI y = new CLI(n);
			y.run();

			entry.getStepInfo().put("volume.mp3gain", "?");
		} else {
			final List<String> meta = new ArrayList<String>();
			meta.add(ConfigManager.MP3GAIN.getAbsolutePath());
			meta.add("/r");
			meta.add(f.getAbsolutePath());
			n.command(meta);

			final CLI y = new CLI(n);

			final GainProcessAdapter g = new GainProcessAdapter();

			y.addProcessListener(g);
			y.run();

			entry.getStepInfo().put("volume.mp3gain", g.getVolume());
		}
		nextStep();

	}

	@Override
	public String getStepResults() {
		return volume != 0 ? "Adjust by " + volume + "dB." : "No adjust needed.";
	}

}
