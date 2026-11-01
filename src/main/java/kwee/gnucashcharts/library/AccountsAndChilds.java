package kwee.gnucashcharts.library;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.druvu.acc.api.AccStore;
import com.druvu.acc.api.entity.Account;

public class AccountsAndChilds {

  private Map<String, Account> accountMap = new HashMap<>();
  private Map<String, ArrayList<String>> subAccountMap = new HashMap<>();
  private AccStore store;

  public AccountsAndChilds(Path filePath) {
    store = AccStore.load(filePath);
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
        // Do nothing
      }
    }
  }

  public Account getAccount(String a_Id) {
    return accountMap.get(a_Id);
  }

  public Account getAccount() {
    return accountMap.get("Root Account");
  }

  public ArrayList<String> getChilds(String a_Id) {
    return subAccountMap.get(a_Id);
  }

  public ArrayList<String> getChilds() {
    return subAccountMap.get("Root Account");
  }

  public ArrayList<Account> getChildAccs() {
    return getChildAccs("Root Account");
  }

  public ArrayList<Account> getChildAccs(String a_Id) {
    ArrayList<Account> l_childAcc = new ArrayList<Account>();
    ArrayList<String> l_childAccId = new ArrayList<String>(subAccountMap.get(a_Id));
    l_childAccId.forEach(childId -> {
      Account l_account = getAccount(childId);
      l_childAcc.add(l_account);
    });
    return l_childAcc;
  }

}
