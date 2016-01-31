// Owner may discard any card

if(target.canDiscard()) {
	if(card.getOwner() == target.getOwner()) {
		valid = true;
	}
}
