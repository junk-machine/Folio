package ru.nahk.folio.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import ru.nahk.folio.R;
import ru.nahk.folio.utils.WindowHelper;
import ru.nahk.folio.validators.DecimalNumberTextValidator;
import ru.nahk.folio.validators.NotEmptyTextValidator;
import ru.nahk.folio.validators.TextValidatorBase;

/**
 * Activity to add new position lot or modify an existing one.
 */
public class EditLotActivity
    extends
        DataAccessActivityBase
    implements
        TextValidatorBase.OnValidationStateChangedListener {

    /**
     * Request code to return result.
     */
    public static final int DEFAULT_REQUEST_CODE = 1;

    /**
     * Identifier of the lot to modify.
     */
    private Long mLotId;

    /**
     * Identifier of the position to add lot for.
     */
    private long mPositionId;

    /**
     * Stock symbol to add lot for.
     */
    private String mStockSymbol;

    /**
     * Flag that indicates whether lots on the parent activity were changed.
     */
    private boolean mLotsChanged;

    /**
     * Text edit control to edit number of shares.
     */
    private EditText mQuantityEditText;

    /**
     * Text edit control to edit stocks purchase price.
     */
    private EditText mPurchasePriceEditText;

    /**
     * Text edit control to edit stocks purchase date.
     */
    private EditText mPurchaseDateEditText;

    /**
     * Text edit control to edit stock purchase commission.
     */
    private EditText mPurchaseCommissionEditText;

    /**
     * Confirmation button.
     */
    private AppCompatButton mOkButton;

    /**
     * Validators for input fields.
     */
    private TextValidatorBase[] mValidators;

    /**
     * Initializes activity view.
     * @param savedInstanceState Saved activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lot);

        mQuantityEditText = findViewById(R.id.quantity_text);
        mPurchasePriceEditText = findViewById(R.id.purchase_price_text);
        mPurchaseDateEditText = findViewById(R.id.purchase_date_text);
        mPurchaseCommissionEditText = findViewById(R.id.commission_text);

        mOkButton = findViewById(R.id.button_ok);

        if (!initializeState(savedInstanceState)
                && (getIntent() == null || !initializeState(getIntent().getExtras()))) {
            setResult(RESULT_CANCELED);
            finish();
        }

        mValidators = new TextValidatorBase[] {
            new NotEmptyTextValidator(
                (TextInputLayout) findViewById(R.id.quantity_text_layout),
                R.string.empty_quantity_error),
            new DecimalNumberTextValidator(
                (TextInputLayout) findViewById(R.id.purchase_price_text_layout),
                4,
                R.string.empty_purchase_price_error,
                R.string.too_many_fraction_digits_error),
            new NotEmptyTextValidator(
                (TextInputLayout) findViewById(R.id.purchase_date_text_layout),
                R.string.empty_purchase_date_error),
            new DecimalNumberTextValidator(
                (TextInputLayout) findViewById(R.id.commission_text_layout),
                4,
                R.string.empty_commission_error,
                R.string.too_many_fraction_digits_error)
        };

        for (TextValidatorBase validator : mValidators) {
            validator.setOnValidationStateChangedListener(this);
        }

        mPurchaseDateEditText.setOnFocusChangeListener(new DatePickerEditTextFocusListener());

        mPurchaseCommissionEditText
            .setOnEditorActionListener(new ImeActionDoneButtonClickDispatcher(mOkButton));

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent result = new Intent();

                // Parent activity state
                result.putExtra(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId);
                result.putExtra(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol);
                result.putExtra(ActivityNavigationConstants.HAS_CHANGED_KEY, mLotsChanged);

                // Lot information
                if (mLotId != null) {
                    result.putExtra(
                        ActivityNavigationConstants.LOT_ID_KEY,
                        mLotId.longValue());
                }

                result.putExtra(
                    ActivityNavigationConstants.STOCK_QUANTITY_KEY,
                    Integer.parseInt(mQuantityEditText.getText().toString()));

                result.putExtra(
                    ActivityNavigationConstants.STOCK_PURCHASE_PRICE_KEY,
                    mPurchasePriceEditText.getText().toString());

                result.putExtra(
                    ActivityNavigationConstants.STOCK_PURCHASE_DATE_KEY,
                    mPurchaseDateEditText.getText().toString());

                result.putExtra(
                    ActivityNavigationConstants.STOCK_PURCHASE_COMMISSION_KEY,
                    mPurchaseCommissionEditText.getText().toString());

                setResult(RESULT_OK, result);
                finish();
            }
        });

        WindowHelper.ensureKeyboard(this, getWindow());
    }

    /**
     * Saves the activity state.
     * @param outState Bundle to save the state to.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mLotId != null) {
            outState.putLong(ActivityNavigationConstants.LOT_ID_KEY, mLotId);
        }

        outState.putLong(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId);
        outState.putString(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol);
        outState.putBoolean(ActivityNavigationConstants.HAS_CHANGED_KEY, mLotsChanged);
    }

    /**
     * Creates the intent to return to parent activity.
     * @return Intent to return to parent activity.
     */
    @Override
    public Intent getParentActivityIntent() {
        Intent parentIntent = super.getParentActivityIntent();

        if (parentIntent != null) {
            parentIntent.putExtra(ActivityNavigationConstants.POSITION_ID_KEY, mPositionId);
            parentIntent.putExtra(ActivityNavigationConstants.STOCK_SYMBOL_KEY, mStockSymbol);
            parentIntent.putExtra(ActivityNavigationConstants.HAS_CHANGED_KEY, mLotsChanged);
        }

        return parentIntent;
    }

    /**
     * Initializes activity from the given state bundle.
     * This can be intent's extra or previously saved state.
     * @param state State bundle.
     * @return True if intent had all required data, otherwise false.
     */
    private boolean initializeState(Bundle state) {
        if (state != null) {
            long lotId = state.getLong(ActivityNavigationConstants.LOT_ID_KEY, -1);
            if (lotId >= 0) {
                mLotId = lotId;
                fillControlsFromState(state);
            }

            mPositionId = state.getLong(ActivityNavigationConstants.POSITION_ID_KEY, -1);
            mStockSymbol = state.getString(ActivityNavigationConstants.STOCK_SYMBOL_KEY);
            mLotsChanged = state.getBoolean(ActivityNavigationConstants.HAS_CHANGED_KEY, false);

            if (mPositionId != -1 && !TextUtils.isEmpty(mStockSymbol)) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(
                        getString(
                            mLotId == null
                                ? R.string.add_lot_title_format
                                : R.string.edit_lot_title_format,
                            mStockSymbol));
                }

                if (mOkButton != null) {
                    mOkButton.setText(
                        mLotId == null
                            ? R.string.add_button
                            : R.string.save_button);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Populates edit controls from the provided state.
     * @param state State bundle.
     */
    private void fillControlsFromState(Bundle state) {
        if (state == null) {
            return;
        }

        if (state.containsKey(ActivityNavigationConstants.STOCK_QUANTITY_KEY)) {
            // Format integer same way as we parse it when returning from activity
            @SuppressWarnings("SetTextI18n")
            String quantityString = Integer.toString(
                state.getInt(ActivityNavigationConstants.STOCK_QUANTITY_KEY, 0));
            mQuantityEditText.setText(quantityString);
        }

        if (state.containsKey(ActivityNavigationConstants.STOCK_PURCHASE_PRICE_KEY)) {
            mPurchasePriceEditText.setText(
                state.getString(ActivityNavigationConstants.STOCK_PURCHASE_PRICE_KEY));
        }

        if (state.containsKey(ActivityNavigationConstants.STOCK_PURCHASE_DATE_KEY)) {
            mPurchaseDateEditText.setText(
                state.getString(ActivityNavigationConstants.STOCK_PURCHASE_DATE_KEY));
        }

        if (state.containsKey(ActivityNavigationConstants.STOCK_PURCHASE_COMMISSION_KEY)) {
            mPurchaseCommissionEditText.setText(
                state.getString(ActivityNavigationConstants.STOCK_PURCHASE_COMMISSION_KEY));
        }
    }

    /**
     * Handles validator state change event.
     * @param isValid Flag that indicates whether input is valid.
     */
    @Override
    public void onValidationStateChanged(boolean isValid) {
        for (TextValidatorBase validator : mValidators) {
            if (!validator.isValid()) {
                mOkButton.setEnabled(false);
                return;
            }
        }

        mOkButton.setEnabled(true);
    }
}
