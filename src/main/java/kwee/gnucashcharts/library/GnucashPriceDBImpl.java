package kwee.gnucashcharts.library;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.druvu.acc.api.AccStore;
import com.druvu.acc.api.entity.Account;
import com.druvu.acc.api.entity.Commodity;
import com.druvu.acc.api.entity.CommodityId;
import com.druvu.acc.api.entity.Price;

import kwee.logger.MyLogger;

public class GnucashPriceDBImpl {
  private static final Logger lOGGER = MyLogger.getLogger();

  private boolean b_PriceDBPresent = false;
  private AccStore store;
  private List<Price> priceDB;
  private File myFile;
  private CommodityId m_rootComId;

  protected static final DateFormat PRICE_QUOTE_DATE_FORMAT = new SimpleDateFormat(
      Const.STANDARD_DATE_FORMAT_BOOK);

  public GnucashPriceDBImpl(File a_myFile) throws IOException {
    this.myFile = a_myFile;
    store = AccStore.load(this.myFile.toPath());
    priceDB = store.prices();

    Optional<Account> rAcc = store.accountByName("Root Account");
    m_rootComId = rAcc.orElse(null).commodity().orElse(null);

    if (!priceDB.isEmpty()) {
      b_PriceDBPresent = true;
    }
  }

  public File getFile() {
    return myFile;
  }

  public List<String> getCommoditieSpaces() {
    List<String> l_CommoditySpaces = new ArrayList<String>();
    if (b_PriceDBPresent) {
      for (Iterator<Price> iter = priceDB.iterator(); iter.hasNext();) {
        Price price = iter.next();
        CommodityId comodity = price.commodity();
        if (!l_CommoditySpaces.contains(comodity.getCmdtySpace())) {
          l_CommoditySpaces.add(comodity.getCmdtySpace());
        }
      }
    }
    return l_CommoditySpaces;
  }

  public List<String> getCommodities() {
    List<String> l_Commodities = new ArrayList<String>();
    if (b_PriceDBPresent) {
      for (Iterator<Price> iter = priceDB.getPrice().iterator(); iter
          .hasNext();) {
        Price price = iter.next();
        Price.PriceCommodity comodity = price.getPriceCommodity();
        if (!l_Commodities.contains(comodity.getCmdtyId())) {
          l_Commodities.add(comodity.getCmdtyId());
        }
      }
    }
    return l_Commodities;
  }

  public List<String> getCommodities(String a_CommoditieSpace) {
    List<String> l_Commodities = new ArrayList<String>();
    if (b_PriceDBPresent) {
      for (Iterator<Price> iter = priceDB.getPrice().iterator(); iter
          .hasNext();) {
        Price price = iter.next();
        Price.PriceCommodity comodity = price.getPriceCommodity();
        if (comodity.getCmdtySpace().equals(a_CommoditieSpace)) {
          if (!l_Commodities.contains(comodity.getCmdtyId())) {
            l_Commodities.add(comodity.getCmdtyId());
          }
        }
      }
    }
    return l_Commodities;
  }

  public FixedPointNumber getPrice(String a_Commodity, LocalDate a_CurDate) {
    if (b_PriceDBPresent) {
      return getPrice("", a_Commodity, a_CurDate, 0);
    }
    return null;
  }

  public FixedPointNumber getPrice(String a_CommoditieSpace, String a_Commodity,
      LocalDate a_CurDate, int depth) {
    Date latestDate = null;
    FixedPointNumber latestQuote = null;
    FixedPointNumber factor = new FixedPointNumber(1); // factor is used if the
                                                       // quote is not to our
                                                       // base-currency
    final int maxRecursionDepth = 5;

    if (b_PriceDBPresent) {
      for (Price priceQuote : (List<Price>) priceDB.getPrice()) {
        try {
          if (priceQuote == null) {
            lOGGER.log(Level.INFO, "gnucash-file contains null price-quotes"
                + " there may be a problem with JWSDP");
            continue;
          }
          if (priceQuote.getPriceCurrency() == null) {
            lOGGER.log(Level.INFO,
                "gnucash-file contains price-quotes" + " with no currency id='"
                    + priceQuote.getPriceId().getValue() + "'");
            continue;
          }
          if (priceQuote.getPriceCurrency().getCmdtyId() == null) {
            lOGGER.log(Level.INFO,
                "gnucash-file contains price-quotes"
                    + " with no currency-id id='"
                    + priceQuote.getPriceId().getValue() + "'");
            continue;
          }
          if (priceQuote.getPriceCurrency().getCmdtySpace() == null) {
            lOGGER.log(Level.INFO,
                "gnucash-file contains price-quotes"
                    + " with no currency-namespace id='"
                    + priceQuote.getPriceId().getValue() + "'");
            continue;
          }
          if (priceQuote.getPriceTime() == null) {
            lOGGER.log(Level.INFO,
                "gnucash-file contains price-quotes" + " with no timestamp id='"
                    + priceQuote.getPriceId().getValue() + "'");
            continue;
          }
          if (priceQuote.getPriceValue() == null) {
            lOGGER.log(Level.INFO,
                "gnucash-file contains price-quotes" + " with no value id='"
                    + priceQuote.getPriceId().getValue() + "'");
            continue;
          }
          /*
           * if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND")
           * && priceQuote.getPriceType() == null) {
           * LOGGER.warn("gnucash-file contains FUND-price-quotes" +
           * " with no type id='" + priceQuote.getPriceId().getValue() + "'");
           * continue; }
           */
          if (!a_CommoditieSpace.isBlank()) {
            if (!priceQuote.getPriceCommodity().getCmdtySpace()
                .equals(a_CommoditieSpace)) {
              continue;
            }
          }
          if (!priceQuote.getPriceCommodity().getCmdtyId()
              .equals(a_Commodity)) {
            continue;
          }
          /*
           * if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND")
           * && (priceQuote.getPriceType() == null ||
           * !priceQuote.getPriceType().equals("last") )) {
           * LOGGER.warn("ignoring FUND-price-quote of unknown type '" +
           * priceQuote.getPriceType() + "' expecting 'last' "); continue; }
           */

          if (!priceQuote.getPriceCurrency().getCmdtySpace()
              .equals("ISO4217")) {
            if (depth > maxRecursionDepth) {
              lOGGER.log(Level.INFO,
                  "ignoring price-quote that is not in an"
                      + " ISO4217 -currency but in '"
                      + priceQuote.getPriceCurrency().getCmdtyId());
              continue;
            }
            factor = getPrice(priceQuote.getPriceCurrency().getCmdtySpace(),
                priceQuote.getPriceCurrency().getCmdtyId(), a_CurDate,
                depth + 1);
          } else {
            if (!priceQuote.getPriceCurrency().getCmdtyId()
                .equals(myFile.getDefaultCurrencyID())) {
              if (depth > maxRecursionDepth) {
                lOGGER.log(Level.INFO,
                    "ignoring price-quote that is not in "
                        + myFile.getDefaultCurrencyID() + " " + "but in  '"
                        + priceQuote.getPriceCurrency().getCmdtyId());
                continue;
              }
              factor = getPrice(priceQuote.getPriceCurrency().getCmdtySpace(),
                  priceQuote.getPriceCurrency().getCmdtyId(), a_CurDate,
                  depth + 1);
            }
          }

          Date date = PRICE_QUOTE_DATE_FORMAT
              .parse(priceQuote.getPriceTime().getTsDate());
          LocalDateTime ls_CurDate = a_CurDate.atTime(23, 59, 59, 999_999_99);
          Date l_CurDate = Date
              .from(ls_CurDate.atZone(ZoneId.systemDefault()).toInstant());

          if (date.before(l_CurDate)) {
            if (latestDate == null || latestDate.before(date)) {
              latestDate = date;
              latestQuote = new FixedPointNumber(priceQuote.getPriceValue());
              LOGGER.debug("getLatestPrice(pCmdtySpace='" + a_CommoditieSpace
                  + "', String pCmdtyId='" + a_Commodity + "') converted "
                  + latestQuote + " <= " + priceQuote.getPriceValue());
            }
          }

        } catch (NumberFormatException e) {
          LOGGER.error("[NumberFormatException] Problem in "
              + getClass().getName() + ".getLatestPrice(pCmdtySpace='"
              + a_CommoditieSpace + "', String pCmdtyId='" + a_Commodity
              + "')! Ignoring a bad price-quote '" + priceQuote + "'", e);
        } catch (ParseException e) {
          LOGGER.error("[ParseException] Problem in " + getClass().getName()
              + ".getLatestPrice(pCmdtySpace='" + a_CommoditieSpace
              + "', String pCmdtyId='" + a_Commodity
              + "')! Ignoring a bad price-quote '" + priceQuote + "'", e);
        } catch (NullPointerException e) {
          LOGGER.error("[NullPointerException] Problem in "
              + getClass().getName() + ".getLatestPrice(pCmdtySpace='"
              + a_CommoditieSpace + "', String pCmdtyId='" + a_Commodity
              + "')! Ignoring a bad price-quote '" + priceQuote + "'", e);
        } catch (ArithmeticException e) {
          LOGGER.error("[ArithmeticException] Problem in "
              + getClass().getName() + ".getLatestPrice(pCmdtySpace='"
              + a_CommoditieSpace + "', String pCmdtyId='" + a_Commodity
              + "')! Ignoring a bad price-quote '" + priceQuote + "'", e);
        }
      }
    }

    MyLogger.log(getClass().getName() + ".getLatestPrice(pCmdtySpace='"
        + a_CommoditieSpace + "', String pCmdtyId='" + a_Commodity + "')= "
        + latestQuote + " from " + latestDate);

    if (latestQuote == null) {
      return new FixedPointNumber(1);
    }

    if (factor == null) {
      factor = new FixedPointNumber(1);
    }

    return factor.multiply(latestQuote);
  }

  public List<Price> getPrices(String a_Commoditie) {
    List<Price> l_Prices = new ArrayList<Price>();
    if (b_PriceDBPresent) {
      for (Iterator<Price> iter = priceDB.getPrice().iterator(); iter
          .hasNext();) {
        Price price = iter.next();
        if (price.getPriceCommodity().getCmdtyId().equals(a_Commoditie)) {
          l_Prices.add(price);
        }
      }
    }
    return l_Prices;
  }
}
