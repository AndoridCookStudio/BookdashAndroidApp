package org.bookdash.android.presentation.listbooks;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.bookdash.android.R;
import org.bookdash.android.domain.pojo.*;

/**
 * @author rebeccafranks
 * @since 15/11/11.
 */
public class BookViewHolder extends RecyclerView.ViewHolder {

    public TextView author;
    public TextView bookTitle;
    public ImageView bookCover;
    public CardView cardContainer;
    public BookDetail bookDetail;
    public ImageView downloadedIcon;

    public BookViewHolder(View v) {
        super(v);
        cardContainer = (CardView) v.findViewById(R.id.card_view);
        bookTitle = (TextView) v.findViewById(R.id.textViewBookName);
        bookCover = (ImageView) v.findViewById(R.id.imageViewBookCover);
        author = (TextView) v.findViewById(R.id.textViewAuthor);
        downloadedIcon = (ImageView) v.findViewById(R.id.imageViewBookDownloaded);
    }
}
