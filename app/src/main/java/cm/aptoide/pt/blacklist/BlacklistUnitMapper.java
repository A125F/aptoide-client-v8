package cm.aptoide.pt.blacklist;

public class BlacklistUnitMapper {

  public BlacklistUnit mapActionCardToBlacklistUnit(String type, String id) {
    switch (type) {
      case "WALLET_ADS_OFFER":
        return new BlacklistUnit(BlacklistManager.BlacklistTypes.WALLET_ADS_OFFER.getType() + id,
            BlacklistManager.BlacklistTypes.WALLET_ADS_OFFER.getMaxPossibleImpressions());
      case "INFO_BUNDLE":
        return new BlacklistUnit(BlacklistManager.BlacklistTypes.APPC_CARD_INFO.getType() + id,
            BlacklistManager.BlacklistTypes.APPC_CARD_INFO.getMaxPossibleImpressions());
      default:
        throw new IllegalArgumentException(
            "Wrong blacklist key. Please, make sure you are passing the correct action card type and id.");
    }
  }
}
