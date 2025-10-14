package cl.numiscoin2.setting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import cl.numiscoin2.R

class FAQExpandableListAdapter(
    private val context: android.content.Context,
    private val faqList: List<FAQActivity.FAQ>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = faqList.size

    override fun getChildrenCount(groupPosition: Int): Int = 1

    override fun getGroup(groupPosition: Int): Any = faqList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = faqList[groupPosition].answer

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view = convertView
        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_group_faq, parent, false)
        }

        val questionTextView = view!!.findViewById<TextView>(R.id.questionTextView)
        val expandIcon = view.findViewById<TextView>(R.id.expandIcon)

        val faq = getGroup(groupPosition) as FAQActivity.FAQ
        questionTextView.text = faq.question
        expandIcon.text = if (isExpanded) "−" else "+"

        // Cambiar estilo cuando está expandido
        if (isExpanded) {
            questionTextView.setTextColor(ContextCompat.getColor(context, R.color.text_light))
            questionTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark))
        } else {
            questionTextView.setTextColor(ContextCompat.getColor(context, R.color.text_light))
            questionTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark))
        }

        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var view = convertView
        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_item_faq, parent, false)
        }

        val answerTextView = view!!.findViewById<TextView>(R.id.answerTextView)
        val answer = getChild(groupPosition, childPosition) as String
        answerTextView.text = answer

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false
}