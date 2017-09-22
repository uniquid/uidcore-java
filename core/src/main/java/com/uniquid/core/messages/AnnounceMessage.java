package com.uniquid.core.messages;

public class AnnounceMessage implements UniquidMessage {
	
	public String publicKey, name, recipe;

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRecipe() {
		return recipe;
	}

	public void setRecipe(String recipe) {
		this.recipe = recipe;
	}

	@Override
	public MessageType getMessageType() {

		return MessageType.ANNOUNCE;

	}
	
}
