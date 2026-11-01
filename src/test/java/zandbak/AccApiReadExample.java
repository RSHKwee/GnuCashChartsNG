package zandbak;

import com.druvu.acc.api.AccStore;
import com.druvu.acc.api.entity.Account;
import com.druvu.acc.api.entity.Price;
import com.druvu.acc.api.entity.Transaction;
import com.druvu.acc.api.service.AccountService;

import kwee.logger.MyLogger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example usage of Acc API.
 *
 * @author Deniss Larka <br>
 *         on 11 jan 2026
 */

public class AccApiReadExample {
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
    new AccApiReadExample().run(filePath);
  }

  void run(Path filePath) {

    AccStore store = AccStore.load(filePath);
    AccountService accountService = AccountService.create(store, "Root Account");
    List<Price> a = store.prices();

    for (var account : store.accounts()) {
      // 'balance' is the account's own splits, 'total' rolls up its sub-accounts -
      // the two
      // columns of the GnuCash account tree. Both carry their commodity: a balance is
      // always
      // one, a total can span several and is printed per commodity rather than
      // collapsed.

      lOGGER.log(Level.INFO, "{" + account + "} balance: {" + accountService.balance(account.id()) + "} total: {"
          + accountService.totalBalance(account.id()) + "}");
      lOGGER.log(Level.FINE, "");
    }

    for (Transaction tx : store.transactions()) {
      lOGGER.log(Level.INFO, "{" + tx.description() + " " + tx.toString() + "}");
      lOGGER.log(Level.FINE, "");
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
        IO.println("%-24s %s root %s".formatted(account.name(), account.type(), rootaccname));

        ArrayList<String> l_childAccIds = subAccountMap.get(account.id());
        l_childAccIds.forEach(childAccId -> {
          Optional<Account> l_acc = store.accountById(childAccId);
          if (l_acc.isPresent()) {
            Account ll_acc = l_acc.orElse(null);
            IO.println(" Sub %-24s %s root %s".formatted(ll_acc.name(), ll_acc.type(), rootaccname));
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
