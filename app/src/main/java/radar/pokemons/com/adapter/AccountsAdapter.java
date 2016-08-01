package radar.pokemons.com.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import radar.pokemons.com.R;
import radar.pokemons.com.model.Account;

public class AccountsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  List<Account> mItems;
  private AccountAdapterListener accountAdapterListener;

  public interface AccountAdapterListener {
    void onAccountDelete(int index);
  }

  public AccountsAdapter(List<Account> accounts, AccountAdapterListener accountAdapterListener) {
    this.mItems = accounts;
    this.accountAdapterListener = accountAdapterListener;
  }

  class AccountItemHolder extends RecyclerView.ViewHolder {

    public TextView accountName;
    public Button deleteBtn;

    public AccountItemHolder(View itemView) {
      super(itemView);
      accountName = (TextView) itemView.findViewById(R.id.account_name);
      deleteBtn = (Button) itemView.findViewById(R.id.delete_account_btn);
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.account_list_item, parent, false);
    return new AccountItemHolder(v);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
    final Account account = mItems.get(position);
    Context context = holder.itemView.getContext();
    AccountItemHolder itemHolder = (AccountItemHolder) holder;
    itemHolder.accountName.setText(account.getUsername());

    itemHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (accountAdapterListener != null) {
          accountAdapterListener.onAccountDelete(position);
        }
      }
    });
  }

  @Override
  public int getItemCount() {
    return mItems.size();
  }
}
