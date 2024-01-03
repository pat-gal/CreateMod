package com.simibubi.create.content.trains.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.network.chat.MutableComponent;

public class GlobalTrainDisplayData {

	public static final Map<String, Collection<TrainDeparturePrediction>> statusByDestination = new HashMap<>();
	public static boolean updateTick = false;

	public static void refresh() {
		statusByDestination.clear();
		for (Train train : Create.RAILWAYS.trains.values()) {
			if (train.runtime.paused || train.runtime.getSchedule() == null)
				continue;
			if (train.derailed || train.graph == null)
				continue;
			for (TrainDeparturePrediction prediction : train.runtime.submitPredictions())
				statusByDestination.computeIfAbsent(prediction.destination, $ -> new ArrayList<>())
					.add(prediction);
		}
	}

	public static List<TrainDeparturePrediction> prepare(String filter, int maxLines) {
		if(filter.matches("[a-zA-Z0-9\\s]*\\*"))
			filter = "\\Q" + filter.replace("*", "\\E.*\\Q") + "\\E";
		else{
			try {
				Pattern.compile(filter);
			} catch (PatternSyntaxException e) {
				filter = filter.isBlank() ? filter : "\\Q" + filter.replace("*", "\\E.*\\Q") + "\\E";
			}
		}



		String finalFilter = filter;
		return statusByDestination.entrySet()
			.stream()
			.filter(e -> e.getKey()
				.matches(finalFilter))
			.flatMap(e -> e.getValue()
				.stream())
			.sorted()
			.limit(maxLines)
			.toList();
	}

	public static class TrainDeparturePrediction implements Comparable<TrainDeparturePrediction> {
		public Train train;
		public int ticks;
		public MutableComponent scheduleTitle;
		public String destination;

		public TrainDeparturePrediction(Train train, int ticks, MutableComponent scheduleTitle, String destination) {
			this.scheduleTitle = scheduleTitle;
			this.destination = destination;
			this.train = train;
			this.ticks = ticks;
		}

		private int getCompareTicks() {
			if (ticks == -1)
				return Integer.MAX_VALUE;
			if (ticks < 200)
				return 0;
			return ticks;
		}

		@Override
		public int compareTo(TrainDeparturePrediction o) {
			int compare = Integer.compare(getCompareTicks(), o.getCompareTicks());
			if (compare == 0)
				return train.name.getString()
					.compareTo(o.train.name.getString());
			return compare;
		}

	}

}
