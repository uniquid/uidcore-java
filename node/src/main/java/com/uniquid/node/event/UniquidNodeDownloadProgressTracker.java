package com.uniquid.node.event;

import java.util.Date;

import org.bitcoinj.core.listeners.DownloadProgressTracker;

import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.node.state.UniquidNodeStateContext;

public class UniquidNodeDownloadProgressTracker extends DownloadProgressTracker {

	private UniquidNodeStateContext nodeStateContext;

	public UniquidNodeDownloadProgressTracker(UniquidNodeStateContext nodeStateContext) {
		super();

		this.nodeStateContext = nodeStateContext;
	}

	@Override
	protected void startDownload(final int blocks) {
		
		for (UniquidNodeEventListener listener : nodeStateContext.getUniquidNodeEventListeners()) {
			listener.onSyncStarted(blocks);
		}

	}

	@Override
	protected void progress(final double pct, final int blocksSoFar, final Date date) {

		for (UniquidNodeEventListener listener : nodeStateContext.getUniquidNodeEventListeners()) {
			listener.onSyncProgress(pct, blocksSoFar, date);
		}
		
	}

	@Override
	public void doneDownload() {
		
		for (UniquidNodeEventListener listener : nodeStateContext.getUniquidNodeEventListeners()) {
			listener.onSyncEnded();
		}

	}

}
