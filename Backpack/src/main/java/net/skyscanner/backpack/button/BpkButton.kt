package net.skyscanner.backpack.button

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.*
import androidx.core.content.ContextCompat
import net.skyscanner.backpack.R
import net.skyscanner.backpack.button.internal.*
import net.skyscanner.backpack.util.use
import net.skyscanner.backpack.util.ResourcesUtil

open class BpkButton : BpkButtonBase {
  constructor(context: Context) : this(context, null)
  constructor(context: Context, type: Type) : this(context, null, getStyle(type), type)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, getStyle(context, attrs))
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, Type.Primary)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, type: Type) :
    super(context, attrs, defStyleAttr) {
    this.initialType = type
    initialize(attrs, defStyleAttr)
  }

  companion object {
    const val START = ICON_POSITION_START
    const val END = ICON_POSITION_END
    const val ICON_ONLY = ICON_POSITION_ICON_ONLY
  }

  @IntDef(START, END, ICON_ONLY)
  annotation class IconPosition

  @BpkButton.IconPosition
  override var iconPosition
    get() = super.iconPosition
    set(value) {
      super.iconPosition = value
    }

  private var initialType: Type

  @ColorInt
  private var buttonBackgroundColor: Int = ContextCompat.getColor(context, R.color.bpkGreen500)

  @ColorInt
  private var buttonStrokeColor: Int = ContextCompat.getColor(context, android.R.color.transparent)

  @ColorInt
  private var buttonStrokeColorPressed: Int = buttonStrokeColor

  @Dimension
  private val paddingHorizontal = tokens.bpkSpacingBase - tokens.bpkSpacingSm

  @Dimension
  private val paddingVertical = tokens.bpkSpacingMd + (tokens.bpkBorderSizeLg / 2)

  @Dimension
  private var defaultCornerRadius = context.resources.getDimension(R.dimen.bpkSpacingLg)

  @Dimension
  private var roundedButtonCorner = defaultCornerRadius

  @Dimension
  private val strokeWidthNormal = tokens.bpkBorderSizeLg

  @Dimension
  private val strokeWidthSelected = tokens.bpkBorderSizeLg + ResourcesUtil.dpToPx(1, context)

  val type: Type
    get() {
      return initialType
    }

  private fun initialize(attrs: AttributeSet?, defStyleAttr: Int) {
    context.theme.obtainStyledAttributes(attrs, R.styleable.BpkButton, defStyleAttr, 0)
      ?.use {
        if (it.hasValue(R.styleable.BpkButton_buttonType)) {
          initialType = Type.fromId(it.getInt(R.styleable.BpkButton_buttonType, 0))
        }

        buttonBackgroundColor = it.getColor(R.styleable.BpkButton_buttonBackgroundColor, ContextCompat.getColor(context, type.bgColor))

        buttonStrokeColor = it.getColor(R.styleable.BpkButton_buttonStrokeColor, ContextCompat.getColor(context, type.strokeColor))
        buttonStrokeColorPressed = it.getColor(R.styleable.BpkButton_buttonStrokeColorPressed, ContextCompat.getColor(context, type.strokeColorSelected))

        roundedButtonCorner = it.getDimension(R.styleable.BpkButton_buttonCornerRadius, defaultCornerRadius)

        defaultTextColor = ContextCompat.getColor(context, type.textColor)
      }

    update()
  }

  override fun update() {
    if (iconPosition == ICON_ONLY) {
      text = ""
    }

    var paddingHorizontal = paddingHorizontal
    val paddingVertical = paddingVertical

    if (iconPosition == ICON_ONLY) {
      paddingHorizontal = tokens.bpkSpacingMd + tokens.bpkBorderSizeLg
    }

    setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)

    if (!text.isNullOrEmpty()) {
      compoundDrawablePadding = tokens.bpkSpacingSm
    }

    background = getButtonBackground()

    if (shouldSetStateListAnimator()) {
      loadStateListAnimator(R.drawable.bpk_button_state_animator)
    }

    clipToOutline = true
  }

  @VisibleForTesting
  internal fun disabledBackground(): Drawable {
    return getRippleDrawable(
      normalColor = tokens.gray100,
      cornerRadius = if (iconPosition != ICON_ONLY) roundedButtonCorner else defaultCornerRadius,
      strokeWidth = null,
      strokeColor = null
    )
  }

  private fun getButtonBackground(): Drawable? {
    return if (this.isEnabled) {
      getRippleDrawable(
        normalColor = buttonBackgroundColor,
        cornerRadius = if (iconPosition != ICON_ONLY) roundedButtonCorner else defaultCornerRadius,
        strokeWidth = strokeWidthForType(type),
        strokeColor = getColorSelector(buttonStrokeColor, buttonStrokeColorPressed, buttonStrokeColor)
      )
    } else disabledBackground()
  }

  private fun strokeWidthForType(type: Type): StrokeWidth? =
    when (type) {
      BpkButton.Type.Secondary, BpkButton.Type.Destructive, BpkButton.Type.Outline ->
        Pair(strokeWidthNormal, strokeWidthSelected)
      else -> null
    }

  private fun shouldSetStateListAnimator() =
    isEnabled && isElevationRequiredForType() && isStateListAnimatorSupported()

  private fun isElevationRequiredForType() = type == Type.Primary || type == Type.Featured

  private fun loadStateListAnimator(@DrawableRes animator: Int) {
    this.stateListAnimator = AnimatorInflater.loadStateListAnimator(context, animator)
  }

  enum class Type(
    internal val id: Int,
    @ColorRes internal val bgColor: Int,
    @ColorRes internal val textColor: Int,
    @ColorRes internal val strokeColor: Int,
    @ColorRes internal val strokeColorSelected: Int
  ) {
    Primary(0, R.color.bpkGreen500, R.color.bpkWhite, android.R.color.transparent, android.R.color.transparent),
    Secondary(1, R.color.bpkWhite, R.color.bpkBlue500, R.color.bpkGray100, R.color.bpkGray100),
    Featured(2, R.color.bpkPink500, R.color.bpkWhite, android.R.color.transparent, android.R.color.transparent),
    Destructive(3, R.color.bpkWhite, R.color.bpkRed500, R.color.bpkGray100, R.color.bpkGray100),
    Outline(4, android.R.color.transparent, R.color.bpkWhite, R.color.bpkWhite, R.color.bpkWhite);

    internal companion object {
      internal fun fromId(id: Int): Type {
        for (f in values()) {
          if (f.id == id) return f
        }
        throw IllegalArgumentException()
      }
    }
  }
}

private fun getStyle(context: Context, attrs: AttributeSet?): Int {
  val attr = context.theme.obtainStyledAttributes(attrs, R.styleable.BpkButton, 0, 0)
  val style = BpkButton.Type.fromId(attr.getInt(R.styleable.BpkButton_buttonType, 0))
  return getStyle(style)
}

private fun getStyle(type: BpkButton.Type): Int {
  return when (type) {
    BpkButton.Type.Primary -> R.attr.bpkButtonPrimaryStyle
    BpkButton.Type.Secondary -> R.attr.bpkButtonSecondaryStyle
    BpkButton.Type.Outline -> R.attr.bpkButtonOutlineStyle
    BpkButton.Type.Featured -> R.attr.bpkButtonFeaturedStyle
    BpkButton.Type.Destructive -> R.attr.bpkButtonDestructiveStyle
  }
}

private fun isStateListAnimatorSupported() =
  Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && !isSpecificDeviceBlackListed()

private fun isSpecificDeviceBlackListed() =
  Build.MANUFACTURER.equals("samsung", true) && Build.MODEL.equals("gt-i9505", true)
