package adf.agent.communication.standard.bundle.information;

import adf.agent.communication.standard.bundle.StandardMessage;
import adf.agent.communication.standard.bundle.StandardMessagePriority;
import adf.component.communication.util.BitOutputStream;
import adf.component.communication.util.BitStreamReader;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageRoad extends StandardMessage {
	private static final int SIZE_ROADID = 32;
	private static final int SIZE_BLOCKADEID = 32;
	private static final int SIZE_COST = 32;
	private static final int SIZE_PASSABLE = 1;
	private static final int SIZE_X = 32;
	private static final int SIZE_Y = 32;

	protected int rawRoadID;
	protected int rawBlockadeID;
	protected EntityID roadID;
	protected EntityID roadBlockadeID;
	protected int blockadeRepairCost;
	protected Boolean roadPassable;
	protected Integer blockadeX;
	protected Integer blockadeY;
	protected boolean isSendBlockadeLocation;

	public MessageRoad(boolean isRadio, @Nonnull Road road, @Nullable Blockade blockade, @Nullable Boolean isPassable, boolean isSendBlockadeLocation) {
		this(isRadio, StandardMessagePriority.NORMAL, road, blockade, isPassable, isSendBlockadeLocation);
	}

	public MessageRoad(boolean isRadio, StandardMessagePriority sendingPriority, @Nonnull Road road, @Nullable Blockade blockade, @Nullable Boolean isPassable, boolean isSendBlockadeLocation) {
		super(isRadio, sendingPriority);
		this.roadID = road.getID();
		if (blockade != null) {
			this.roadBlockadeID = blockade.getID();
			this.blockadeRepairCost = blockade.isRepairCostDefined() ? blockade.getRepairCost() : -1;
			this.blockadeX = (isSendBlockadeLocation && blockade.isXDefined()) ? blockade.getX() : null;
			this.blockadeY = (isSendBlockadeLocation && blockade.isYDefined()) ? blockade.getY() : null;
		} else {
			this.roadBlockadeID = null;
			this.rawBlockadeID = -1;
			this.blockadeRepairCost = -1;
			this.blockadeX = null;
			this.blockadeY = null;
		}
		this.roadPassable = isPassable;
		this.isSendBlockadeLocation = isSendBlockadeLocation;
	}

	public MessageRoad(boolean isRadio, int from, int ttl, @Nonnull BitStreamReader bitStreamReader) {
		super(isRadio, from, ttl, bitStreamReader);
		this.rawRoadID = bitStreamReader.getBits(SIZE_ROADID);
		this.rawBlockadeID = (bitStreamReader.getBits(1) == 1) ? bitStreamReader.getBits(SIZE_BLOCKADEID) : -1;
		this.blockadeRepairCost = (bitStreamReader.getBits(1) == 1) ? bitStreamReader.getBits(SIZE_COST) : -1;
		this.blockadeX = (bitStreamReader.getBits(1) == 1) ? bitStreamReader.getBits(SIZE_X) : null;
		this.blockadeY = (bitStreamReader.getBits(1) == 1) ? bitStreamReader.getBits(SIZE_Y) : null;
		this.roadPassable = (bitStreamReader.getBits(1) == 1) ? (0 != bitStreamReader.getBits(SIZE_PASSABLE)) : null;
	}

	@Nonnull
	public EntityID getRoadID() {
		if (this.roadID == null) {
			this.roadID = new EntityID(this.rawRoadID);
		}
		return this.roadID;
	}

	@Nullable
	public EntityID getBlockadeID() {
		if (this.roadBlockadeID == null) {
			if (this.rawBlockadeID != -1) this.roadBlockadeID = new EntityID(this.rawBlockadeID);
		}
		return this.roadBlockadeID;
	}

	public int getRepairCost() {
		return this.blockadeRepairCost;
	}

	public Integer getBlockadeX() {
		return this.blockadeX;
	}

	public Integer getBlockadeY() {
		return this.blockadeY;
	}

	public Boolean isPassable() {
		return this.roadPassable;
	}

	public boolean isBlockadeDefined() {
		return this.getBlockadeID() != null;
	}

	public boolean isRepairCostDefined() {
		return this.blockadeRepairCost != -1;
	}

	public boolean isXDefined() {
		return this.blockadeX != null;
	}

	public boolean isYDefined() {
		return this.blockadeY != null;
	}

	@Override
	public int getByteArraySize() {
		return this.toBitOutputStream().size();
	}

	@Override
	@Nonnull
	public byte[] toByteArray() {
		return this.toBitOutputStream().toByteArray();
	}

	@Override
	@Nonnull
	public BitOutputStream toBitOutputStream() {
		BitOutputStream bitOutputStream = new BitOutputStream();
		bitOutputStream.writeBits(this.roadID.getValue(), SIZE_ROADID);
		if (this.roadBlockadeID != null) {
			bitOutputStream.writeBitsWithExistFlag(this.roadBlockadeID.getValue(), SIZE_BLOCKADEID);
		} else if (this.rawBlockadeID != -1) {
			bitOutputStream.writeBitsWithExistFlag(this.rawBlockadeID, SIZE_BLOCKADEID);
		} else {
			bitOutputStream.writeNullFlag();
		}

		if (this.blockadeRepairCost != -1) {
			bitOutputStream.writeBitsWithExistFlag(this.blockadeRepairCost, SIZE_COST);
		} else {
			bitOutputStream.writeNullFlag();
		}

		if (this.isSendBlockadeLocation) {
			if (this.blockadeX != null) {
				bitOutputStream.writeBitsWithExistFlag(this.blockadeX, SIZE_X);
			} else {
				bitOutputStream.writeNullFlag();
			}

			if (this.blockadeY != null) {
				bitOutputStream.writeBitsWithExistFlag(this.blockadeY, SIZE_Y);
			} else {
				bitOutputStream.writeNullFlag();
			}
		} else {
			bitOutputStream.writeNullFlag(); // for blockadeX
			bitOutputStream.writeNullFlag(); // for blockadeY
		}

		if (this.roadPassable != null) {
			bitOutputStream.writeBitsWithExistFlag((this.roadPassable ? 1 : 0), SIZE_PASSABLE);
		} else {
			bitOutputStream.writeNullFlag();
		}

		return bitOutputStream;
	}

	@Override
	@Nonnull
	public String getCheckKey() {
		return getClass().getCanonicalName() + " > road:" + this.getRoadID().getValue();
	}
}

