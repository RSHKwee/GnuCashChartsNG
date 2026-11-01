package zandbak;

import com.druvu.acc.api.AccStore;
import com.druvu.acc.api.entity.Account;
import com.druvu.acc.api.entity.AccountType;
import com.druvu.acc.api.entity.Amount;
import com.druvu.acc.api.entity.CommodityId;
import com.druvu.acc.api.entity.Price;
import com.druvu.acc.api.entity.Transaction;
import com.druvu.acc.api.service.AccountService;

import kwee.logger.MyLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example usage of Acc API.
 *
 * @author Deniss Larka <br>
 *         on 11 jan 2026
 */

public class AccApiReadExampleStock {
  private static final Logger lOGGER = MyLogger.getLogger();
  private static String gcshFileName = "D:\\Users\\René\\SynologyDrive\\Documenten\\Administraties\\Prive_RK.gnucash";

  static void main(String[] args) {
    /* @formatter:off
    if (args.length < 1) {
      System.err.println("Usage: AccApiExample <gnucash-file>");
      System.exit(1);
    }
    @formatter:on
    */
    Path filePath = Paths.get(gcshFileName);
    new AccApiReadExampleStock().run(filePath);
  }

  void run(Path filePath) {

    AccStore store = AccStore.load(filePath);
    AccountService accountService = AccountService.create(store, "Root Account");
    List<Price> dbPrices = store.prices();
    // Account assets = accountService.accountByName("Fondsen");
    List<CommodityId> cid = store.commodities();

    Optional<Account> rAcc = store.accountByName("Root Account");
    CommodityId rComId = rAcc.orElse(null).commodity().orElse(null);

    Set<CommodityId> cidp = new HashSet<>();
    dbPrices.forEach(dbp -> {
      cidp.add(dbp.commodity());
    });

    for (var account : store.accounts()) {
      if ((account.type() == AccountType.MUTUAL || account.type() == AccountType.STOCK)) {
        IO.println("%-24s %s id %s".formatted(account.name(), account.type(), account.id()));
      }
    }

    for (var price : store.prices()) {
      IO.println("%-24s %s id %s".formatted(price.id(), price.commodity(), price.time()));
    }

    Map<String, Account> accountMap = new HashMap<>();
    Map<String, ArrayList<String>> subAccountMap = new HashMap<>();
    for (var account : store.accounts()) {
      accountMap.put(account.id(), account);
      ArrayList<String> l_childAccs = new ArrayList<String>();
      subAccountMap.put(account.id(), l_childAccs);
    }

    for (var account : store.accounts()) {
      String rootaccname = "";
      try {
        Account rootacc = accountMap.get(account.parentId().orElse(""));
        rootaccname = rootacc.name();
        if (!rootaccname.isEmpty()) {
          ArrayList<String> l_childAccs = subAccountMap.get(rootacc.id());
          l_childAccs.add(account.id());
          subAccountMap.put(rootacc.id(), l_childAccs);
        }
      } catch (Exception e) {

      }
    }

    for (var account : store.accounts()) {
      final String rootaccname;
      Account rootacc;
      try {
        rootacc = accountMap.get(account.parentId().orElse(""));
        rootaccname = rootacc.name();
        Amount abal = accountService.balance(account.id());
        IO.println("%-24s %s id %s Eur %s".formatted(account.name(), account.type(), account.id(), abal));

        ArrayList<String> l_childAccIds = subAccountMap.get(account.id());
        l_childAccIds.forEach(childAccId -> {
          Optional<Account> l_acc = store.accountById(childAccId);
          if (l_acc.isPresent()) {
            Account ll_acc = l_acc.orElse(null);
            Amount abala = accountService.balance(ll_acc.id());
            IO.println(" Sub %-24s %s root %s Eur %s".formatted(ll_acc.name(), ll_acc.type(), rootaccname, abala));
          }
        });
      } catch (Exception e) {

      }
    }

    // for (var tx : store.transactions()) {
    // IO.println("%s %s".formatted(tx.datePosted(), tx.description()));
    // }
  }
}
