package kwee.gnucashcharts.library;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.druvu.acc.api.AccStore;
import com.druvu.acc.api.entity.Account;
import com.druvu.acc.api.entity.Amount;
import com.druvu.acc.api.entity.CommodityId;
import com.druvu.acc.api.entity.Price;

import java.util.logging.Level;
import java.util.logging.Logger;

import kwee.logger.MyLogger;

public class PriceDB {
  private static final Logger lOGGER = MyLogger.getLogger();

  private boolean b_PriceDBPresent = false;
  private AccStore store;
  private List<Price> priceDB;
  private File myFile;
  private CommodityId m_rootComId;

  // protected static final SimpleDateFormat PRICE_QUOTE_DATE_FORMAT = new
  // SimpleDateFormat(Const.STANDARD_DATE_FORMAT_BOOK);
  protected static final DateTimeFormatter PRICE_QUOTE_DATE_FORMAT = DateTimeFormatter
      .ofPattern(Const.STANDARD_DATE_FORMAT_BOOK);

  public PriceDB(File a_myFile) throws IOException {
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
        CommodityId comodityId = price.commodity();
        if (!l_CommoditySpaces.contains(comodityId.namespace())) {
          l_CommoditySpaces.add(comodityId.namespace());
        }
      }
    }
    return l_CommoditySpaces;
  }

  public List<String> getCommodities() {
    List<String> l_Commodities = new ArrayList<String>();
    if (b_PriceDBPresent) {
      for (Iterator<Price> iter = priceDB.iterator(); iter.hasNext();) {
        Price price = iter.next();
        CommodityId comodity = price.commodity();
        if (!l_Commodities.contains(comodity.id())) {
          l_Commodities.add(comodity.id());
        }
      }
    }
    return l_Commodities;
  }

  public List<String> getCommodities(String a_CommoditieSpace) {
    List<String> l_Commodities = new ArrayList<String>();
    if (b_PriceDBPresent) {
      for (Iterator<Price> iter = priceDB.iterator(); iter.hasNext();) {
        Price price = iter.next();
        CommodityId comodity = price.commodity();
        if (comodity.namespace().equals(a_CommoditieSpace)) {
          if (!l_Commodities.contains(comodity.namespace())) {
            l_Commodities.add(comodity.namespace());
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
    return new FixedPointNumber("0");
  }

  public FixedPointNumber getPrice(String a_CommoditieSpace, String a_Commodity, LocalDate a_CurDate, int depth) {
    Date latestDate = null;
    Amount latestQuote = null;
    FixedPointNumber factor = new FixedPointNumber("1"); // factor is used if the quote is not to our base-currency
    final int maxRecursionDepth = 5;

    if (b_PriceDBPresent) {
      for (Price priceQuote : (List<Price>) priceDB) {
        try {
          if (priceQuote == null) {
            lOGGER.log(Level.INFO, "gnucash-file contains null price-quotes" + " there may be a problem with JWSDP");
            continue;
          }
          if (priceQuote.currency() == null) {
            lOGGER.log(Level.INFO,
                "gnucash-file contains price-quotes" + " with no currency id='" + priceQuote.currency().id() + "'");
            continue;
          }
          if (priceQuote.currency().id() == null) {
            lOGGER.log(Level.INFO,
                "gnucash-file contains price-quotes" + " with no currency-id id='" + priceQuote.currency().id() + "'");
            continue;
          }
          if (priceQuote.currency().namespace() == null) {
            lOGGER.log(Level.INFO, "gnucash-file contains price-quotes" + " with no currency-namespace id='"
                + priceQuote.currency().namespace() + "'");
            continue;
          }
          if (priceQuote.time() == null) {
            lOGGER.log(Level.INFO, "gnucash-file contains price-quotes" + " with no timestamp id='"
                + priceQuote.time().format(PRICE_QUOTE_DATE_FORMAT) + "'");
            continue;
          }
          if (priceQuote.value() == null) {
            lOGGER.log(Level.INFO,
                "gnucash-file contains price-quotes" + " with no value id='" + priceQuote.id() + "'");
            continue;
          }
          /*
           * if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND") &&
           * priceQuote.getPriceType() == null) {
           * LOGGER.warn("gnucash-file contains FUND-price-quotes" + " with no type id='"
           * + priceQuote.getPriceId().getValue() + "'"); continue; }
           */
          if (!a_CommoditieSpace.isBlank()) {
            if (!priceQuote.commodity().namespace().equals(a_CommoditieSpace)) {
              continue;
            }
          }
          if (!priceQuote.commodity().id().equals(a_Commodity)) {
            continue;
          }
          /*
           * if (priceQuote.getPriceCommodity().getCmdtySpace().equals("FUND") &&
           * (priceQuote.getPriceType() == null ||
           * !priceQuote.getPriceType().equals("last") )) {
           * LOGGER.warn("ignoring FUND-price-quote of unknown type '" +
           * priceQuote.getPriceType() + "' expecting 'last' "); continue; }
           */

          if (!priceQuote.currency().namespace().equals("ISO4217")) {
            if (depth > maxRecursionDepth) {
              lOGGER.log(Level.INFO, "ignoring price-quote that is not in an" + " ISO4217 -currency but in '"
                  + priceQuote.currency().id());
              continue;
            }
            factor = getPrice(priceQuote.currency().namespace(), priceQuote.currency().id(), a_CurDate, depth + 1);
          } else {
            if (!priceQuote.currency().equals(m_rootComId)) {
              if (depth > maxRecursionDepth) {
                lOGGER.log(Level.INFO, "ignoring price-quote that is not in " + m_rootComId.id() + " " + "but in  '"
                    + priceQuote.currency().id());
                continue;
              }
              factor = getPrice(priceQuote.commodity().namespace(), priceQuote.currency().id(), a_CurDate, depth + 1);
            }
          }

          Date date = Date.from(priceQuote.time().atZone(ZoneId.systemDefault()).toInstant());
          // Date date = PRICE_QUOTE_DATE_FORMAT.parse(pricedate);
          LocalDateTime ls_CurDate = a_CurDate.atTime(23, 59, 59, 999_999_99);
          Date l_CurDate = Date.from(ls_CurDate.atZone(ZoneId.systemDefault()).toInstant());

          if (date.before(l_CurDate)) {
            if (latestDate == null || latestDate.before(date)) {
              latestDate = date;
              latestQuote = new Amount(priceQuote.value(), m_rootComId);
              lOGGER.log(Level.FINE, "getLatestPrice(pCmdtySpace='" + a_CommoditieSpace + "', String pCmdtyId='"
                  + a_Commodity + "') converted " + latestQuote + " <= " + priceQuote.value());
            }
          }

        } catch (NumberFormatException e) {
          lOGGER.log(Level.INFO,
              "[NumberFormatException] Problem in " + getClass().getName() + ".getLatestPrice(pCmdtySpace='"
                  + a_CommoditieSpace + "', String pCmdtyId='" + a_Commodity + "')! Ignoring a bad price-quote '"
                  + priceQuote + "'",
              e);
          // } catch (ParseException e) {
          // LOGGER.error("[ParseException] Problem in " + getClass().getName() +
          // ".getLatestPrice(pCmdtySpace='"
          // + a_CommoditieSpace + "', String pCmdtyId='" + a_Commodity + "')! Ignoring a
          // bad price-quote '"
          // + priceQuote + "'", e);
        } catch (NullPointerException e) {
          lOGGER.log(Level.INFO,
              "[NullPointerException] Problem in " + getClass().getName() + ".getLatestPrice(pCmdtySpace='"
                  + a_CommoditieSpace + "', String pCmdtyId='" + a_Commodity + "')! Ignoring a bad price-quote '"
                  + priceQuote + "'",
              e);
        } catch (ArithmeticException e) {
          lOGGER.log(Level.INFO,
              "[ArithmeticException] Problem in " + getClass().getName() + ".getLatestPrice(pCmdtySpace='"
                  + a_CommoditieSpace + "', String pCmdtyId='" + a_Commodity + "')! Ignoring a bad price-quote '"
                  + priceQuote + "'",
              e);
        }
      }
    }

    lOGGER.log(Level.FINE, getClass().getName() + ".getLatestPrice(pCmdtySpace='" + a_CommoditieSpace
        + "', String pCmdtyId='" + a_Commodity + "')= " + latestQuote + " from " + latestDate);

    if (latestQuote == null) {
      FixedPointNumber ll = new FixedPointNumber("0");
      return ll;
    }

    if (factor == null) {
      factor = new FixedPointNumber("1");
    }
    FixedPointNumber rVal = factor.multiply(latestQuote.value());
    return rVal;
  }

  public List<Price> getPrices(String a_Commoditie) {
    List<Price> l_Prices = new ArrayList<Price>();
    if (b_PriceDBPresent) {
      for (Iterator<Price> iter = priceDB.iterator(); iter.hasNext();) {
        Price price = iter.next();
        if (price.commodity().id().equals(a_Commoditie)) {
          l_Prices.add(price);
        }
      }
    }
    return l_Prices;
  }

}
